import os
import json
import logging
import time
from typing import Dict, List, Optional
from urllib.parse import urlparse
import re

import requests
from flask import Flask, request, Response, jsonify
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
from prometheus_flask_exporter import PrometheusMetrics
from pydantic import BaseModel, validator
from yandex_music import Client
from yandex_music.exceptions import YandexMusicError

# Настройка логирования
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Настройка логирования для flask-limiter
limiter_logger = logging.getLogger('flask-limiter')
limiter_logger.setLevel(logging.INFO)

# Создание Flask приложения
app = Flask(__name__)

# Настройка rate limiting
limiter = Limiter(
    app=app,
    key_func=get_remote_address,
    default_limits=["200 per day", "50 per hour"],
    storage_uri="memory://",
    strategy="fixed-window"
)

# Настройка метрик Prometheus
metrics = PrometheusMetrics(app)

# Конфигурация
YANDEX_TOKEN = os.getenv('YANDEX_TOKEN')
if not YANDEX_TOKEN:
    logger.error("YANDEX_TOKEN не установлен")
    raise ValueError("YANDEX_TOKEN environment variable is required")

# Модели данных
class PlaylistRequest(BaseModel):
    url: str
    
    @validator('url')
    def validate_url(cls, v):
        if not v.startswith('https://music.yandex.ru/'):
            raise ValueError('URL должен быть с домена music.yandex.ru')
        return v

class TrackRequest(BaseModel):
    track_id: int
    
    @validator('track_id')
    def validate_track_id(cls, v):
        if v <= 0:
            raise ValueError('track_id должен быть положительным числом')
        return v

class TrackDownloadRequest(BaseModel):
    track_id: int
    codec: Optional[str] = None
    bitrate: Optional[int] = None
    
    @validator('track_id')
    def validate_track_id(cls, v):
        if v <= 0:
            raise ValueError('track_id должен быть положительным числом')
        return v

def parse_playlist_url(url: str) -> tuple[str, int]:
    """Парсит URL плейлиста и возвращает логин и ID плейлиста"""
    try:
        parsed = urlparse(url)
        match = re.search(r'/users/([^/]+)/playlists/(\d+)', parsed.path)
        if not match:
            raise ValueError("Неверный формат URL плейлиста")
        return match.group(1), int(match.group(2))
    except Exception as e:
        logger.error(f"Ошибка парсинга URL плейлиста: {url}, ошибка: {e}")
        raise ValueError(f"Неверный формат URL плейлиста: {e}")

def get_yandex_client() -> Client:
    """Создает и инициализирует клиент Яндекс.Музыки"""
    try:
        client = Client(YANDEX_TOKEN).init()
        return client
    except Exception as e:
        logger.error(f"Ошибка инициализации клиента Яндекс.Музыки: {e}")
        raise

@app.errorhandler(Exception)
def handle_exception(e):
    """Глобальный обработчик исключений"""
    logger.error(f"Необработанная ошибка: {e}", exc_info=True)
    return jsonify({
        'error': 'Внутренняя ошибка сервера',
        'message': str(e) if app.debug else 'Что-то пошло не так'
    }), 500

@app.errorhandler(ValueError)
def handle_value_error(e):
    """Обработчик ошибок валидации"""
    logger.warning(f"Ошибка валидации: {e}")
    return jsonify({
        'error': 'Ошибка валидации',
        'message': str(e)
    }), 400

@app.errorhandler(YandexMusicError)
def handle_yandex_error(e):
    """Обработчик ошибок API Яндекс.Музыки"""
    logger.error(f"Ошибка API Яндекс.Музыки: {e}")
    return jsonify({
        'error': 'Ошибка API Яндекс.Музыки',
        'message': str(e)
    }), 503

@app.route('/health', methods=['GET'])
@limiter.exempt
def health_check():
    """Health check endpoint"""
    try:
        # Простая проверка - сервис работает
        return jsonify({
            'status': 'healthy', 
            'timestamp': time.time(),
            'service': 'yanmusic-sidecar',
            'version': '1.0.0'
        }), 200
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        return jsonify({'status': 'unhealthy', 'error': str(e)}), 503

@app.route('/health/deep', methods=['GET'])
@limiter.exempt
def deep_health_check():
    """Deep health check с проверкой API Яндекса"""
    try:
        # Проверяем подключение к API
        client = get_yandex_client()
        # Простая проверка - получаем информацию о пользователе
        me = client.me
        if me is None:
            raise Exception("Не удалось получить информацию о пользователе")
        return jsonify({
            'status': 'healthy', 
            'timestamp': time.time(),
            'yandex_api': 'connected',
            'user': me.account.login if hasattr(me, 'account') else 'unknown'
        }), 200
    except Exception as e:
        logger.error(f"Deep health check failed: {e}")
        return jsonify({'status': 'unhealthy', 'error': str(e)}), 503

@app.route('/fetch_playlist', methods=['POST'])
@limiter.limit("10 per minute")
def fetch_playlist():
    """Получает информацию о плейлисте"""
    start_time = time.time()
    
    try:
        # Валидация входных данных
        data = request.get_json()
        if not data:
            return jsonify({'error': 'Отсутствуют данные'}), 400
            
        playlist_request = PlaylistRequest(**data)
        
        logger.info(f"Запрос плейлиста: {playlist_request.url}")
        
        # Парсинг URL
        login, playlist_id = parse_playlist_url(playlist_request.url)
        
        # Получение данных плейлиста
        client = get_yandex_client()
        playlist = client.users_playlists(playlist_id, user_id=login)
        
        # Формирование ответа
        playlist_data = {
            'playlist_id': playlist.kind,
            'title': playlist.title,
            'created': playlist.created,
            'modified': playlist.modified,
            'tracks': []
        }
        
        for track_short in playlist.tracks:
            track = track_short.track
            playlist_data['tracks'].append({
                'track_id': track.id,
                'title': track.title,
                'artists': [artist.name for artist in track.artists],
                'added_at': track_short.timestamp
            })
        
        duration = time.time() - start_time
        logger.info(f"Плейлист получен успешно: {playlist.title}, треков: {len(playlist_data['tracks'])}, время: {duration:.2f}с")
        
        return Response(
            json.dumps(playlist_data, ensure_ascii=False),
            mimetype='application/json'
        )
        
    except Exception as e:
        duration = time.time() - start_time
        logger.error(f"Ошибка получения плейлиста: {e}, время: {duration:.2f}с")
        raise

@app.route('/download_track', methods=['POST'])
@limiter.limit("20 per minute")
def download_track():
    """Получает ссылки для скачивания трека"""
    start_time = time.time()
    
    try:
        # Валидация входных данных
        data = request.get_json()
        if not data:
            return jsonify({'error': 'Отсутствуют данные'}), 400
            
        track_request = TrackRequest(**data)
        
        logger.info(f"Запрос ссылок для скачивания трека: {track_request.track_id}")
        
        # Получение информации о треке
        client = get_yandex_client()
        track = client.tracks(track_request.track_id)[0]
        download_info_list = track.get_download_info()
        
        # Формирование списка ссылок
        urls = [
            {
                'codec': info.codec,
                'bitrate_in_kbps': info.bitrate_in_kbps,
                'download_url': info.get_direct_link()
            }
            for info in download_info_list
        ]
        
        duration = time.time() - start_time
        logger.info(f"Ссылки для трека получены: {track.title}, вариантов: {len(urls)}, время: {duration:.2f}с")
        
        return Response(
            json.dumps({'download_urls': urls}, ensure_ascii=False),
            mimetype='application/json'
        )
        
    except Exception as e:
        duration = time.time() - start_time
        logger.error(f"Ошибка получения ссылок для трека: {e}, время: {duration:.2f}с")
        raise

@app.route('/download_track_content', methods=['POST'])
@limiter.limit("5 per minute")
def download_track_content():
    """Скачивает и возвращает содержимое трека"""
    start_time = time.time()
    
    try:
        # Валидация входных данных
        data = request.get_json()
        if not data:
            return jsonify({'error': 'Отсутствуют данные'}), 400
            
        download_request = TrackDownloadRequest(**data)
        
        logger.info(f"Запрос скачивания трека: {download_request.track_id}")
        
        # Получение информации о треке
        client = get_yandex_client()
        track = client.tracks(download_request.track_id)[0]
        download_info = track.get_download_info()
        
        # Выбор лучшего качества или указанного
        if download_request.codec and download_request.bitrate:
            full_track_info = next(
                (info for info in download_info 
                 if info.codec == download_request.codec and info.bitrate_in_kbps == download_request.bitrate),
                None
            )
            if not full_track_info:
                logger.warning(f"Запрошенное качество не найдено: {download_request.codec}, {download_request.bitrate}")
        else:
            full_track_info = max(download_info, key=lambda info: info.bitrate_in_kbps)
        
        if not full_track_info:
            raise ValueError("Не удалось найти информацию для скачивания")
            
        download_url = full_track_info.get_direct_link()
        
        # Скачивание файла
        logger.info(f"Начинаем скачивание: {track.title}, качество: {full_track_info.codec} {full_track_info.bitrate_in_kbps}kbps")
        
        response = requests.get(download_url, timeout=30)
        response.raise_for_status()
        
        duration = time.time() - start_time
        logger.info(f"Трек скачан успешно: {track.title}, размер: {len(response.content)} байт, время: {duration:.2f}с")
        
        # Определение MIME типа
        mime_type = 'audio/mpeg'
        if full_track_info.codec == 'aac':
            mime_type = 'audio/aac'
        elif full_track_info.codec == 'flac':
            mime_type = 'audio/flac'
        
        # Формирование имени файла
        safe_title = "".join(c for c in track.title if c.isalnum() or c in (' ', '-', '_')).rstrip()
        filename = f"{safe_title}.{full_track_info.codec}"
        
        return Response(
            response.content,
            mimetype=mime_type,
            headers={
                'Content-Disposition': f'attachment; filename="{filename}"',
                'Content-Length': str(len(response.content))
            }
        )
        
    except requests.RequestException as e:
        duration = time.time() - start_time
        logger.error(f"Ошибка скачивания трека: {e}, время: {duration:.2f}с")
        return jsonify({'error': 'Ошибка скачивания файла', 'message': str(e)}), 503
    except Exception as e:
        duration = time.time() - start_time
        logger.error(f"Ошибка обработки запроса скачивания: {e}, время: {duration:.2f}с")
        raise

@app.route('/metrics')
@limiter.exempt
def metrics_endpoint():
    """Endpoint для метрик Prometheus"""
    return metrics.generate_latest()

if __name__ == '__main__':
    # Для разработки
    app.run(host="0.0.0.0", port=5000, debug=False)
else:
    # Для продакшена с gunicorn
    pass

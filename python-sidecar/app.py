from flask import Flask, request, Response
from yandex_music import Client
import re
from urllib.parse import urlparse
import os
import json
import requests
from flask import send_file
from io import BytesIO

YANDEX_TOKEN = os.getenv('YANDEX_TOKEN')

app = Flask(__name__)

def parse_playlist_url(url):
    parsed = urlparse(url)
    match = re.search(r'/users/([^/]+)/playlists/(\d+)', parsed.path)
    if not match:
        raise ValueError("Invalid playlist url")
    return match.group(1), int(match.group(2))

@app.route('/fetch_playlist', methods=['POST'])
def fetch_playlist():
    data = request.json
    url = data['url']
    login, playlist_id = parse_playlist_url(url)
    client = Client(YANDEX_TOKEN).init()
    playlist = client.users_playlists(playlist_id, user_id=login)

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

    return Response(
        json.dumps(playlist_data, ensure_ascii=False),
        mimetype='application/json'
    )

@app.route('/download_track', methods=['POST'])
def download_track():
    data = request.json
    track_id = data['track_id']

    client = Client(YANDEX_TOKEN).init()
    track = client.tracks(track_id)[0]
    download_info_list = track.get_download_info()

    # Собираем все ссылки с их параметрами
    urls = [
        {
            'codec': info.codec,
            'bitrate_in_kbps': info.bitrate_in_kbps,
            'download_url': info.get_direct_link()
        }
        for info in download_info_list
    ]

    return Response(
        json.dumps({'download_urls': urls}, ensure_ascii=False),
        mimetype='application/json'
    )



@app.route('/download_track_content', methods=['POST'])
def download_track_content():
    data = request.json
    track_id = data['track_id']

    client = Client(YANDEX_TOKEN).init()
    track = client.tracks(track_id)[0]
    download_info = track.get_download_info()
    full_track_info = max(download_info, key=lambda info: info.bitrate_in_kbps)
    download_url = full_track_info.get_direct_link()

    # Скачиваем сами
    r = requests.get(download_url)
    r.raise_for_status()

    # Отдаём байты как файл (можно ещё поставить правильный content-type)
    return send_file(
        BytesIO(r.content),
        as_attachment=True,
        download_name=f"{track.title}.mp3",
        mimetype='audio/mpeg'
    )


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000)

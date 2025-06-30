# YanMusic Playlist Tracker Bot

### Описание

Бот для отслеживания изменений в плейлистах Яндекс Музыки и уведомления через Telegram.

### Стек

- Java 21
- Spring Boot 3.2.3
- PostgreSQL
- Telegram Bots API
- Yandex Music API (неофициальный)

### Запуск

1️⃣ Поднять PostgreSQL:
docker-compose up -d

2️⃣ Заполнить переменные окружения:
- TELEGRAM_BOT_USERNAME
- TELEGRAM_BOT_TOKEN
- YANDEX_MUSIC_TOKEN

3️⃣ Собрать проект:
mvn clean install

4️⃣ Собрать Docker:
docker build -t yanmusic-tracker .
docker run --env-file .env -p 8080:8080 yanmusic-tracker

### Основные команды бота

- /subscribe <playlist_url> — подписаться на плейлист в текущем чате


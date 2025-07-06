FROM python:3.11-slim

# Установка системных зависимостей
RUN apt-get update && apt-get install -y \
    gcc \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Создание пользователя для безопасности
RUN groupadd -r app && useradd -r -g app app

# Установка рабочей директории
WORKDIR /app

# Копирование файлов зависимостей
COPY requirements.txt .

# Установка Python зависимостей
RUN pip install --no-cache-dir -r requirements.txt

# Копирование исходного кода
COPY app.py .
COPY gunicorn.conf.py .

# Создание директории для логов
RUN mkdir -p /app/logs && chown -R app:app /app

# Переключение на пользователя app
USER app

# Открытие порта
EXPOSE 5000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:5000/health || exit 1

# Запуск с gunicorn
CMD ["gunicorn", "--config", "gunicorn.conf.py", "app:app"]

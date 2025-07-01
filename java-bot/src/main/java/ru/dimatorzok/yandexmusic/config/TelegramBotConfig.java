package ru.dimatorzok.yandexmusic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import ru.dimatorzok.yandexmusic.telegram.YanMusicBot;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
@Configuration
@Profile("!test")
public class TelegramBotConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final TelegramBotsLongPollingApplication telegramBotsLongPollingApplication;

    private final YanMusicBot yanMusicBot;

    @SneakyThrows
    @PostConstruct
    public void startBots() {

        telegramBotsLongPollingApplication.registerBot(botToken, yanMusicBot);
    }

    @SneakyThrows
    @PreDestroy
    public void stopBots() {

        telegramBotsLongPollingApplication.stop();
    }

}

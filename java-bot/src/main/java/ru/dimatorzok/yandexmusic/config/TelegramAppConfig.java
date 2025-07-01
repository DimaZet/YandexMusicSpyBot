package ru.dimatorzok.yandexmusic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
@Profile("!test")
public class TelegramAppConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsLongPollingApplication() {

        return new TelegramBotsLongPollingApplication();
    }

    @Bean
    public TelegramClient telegramClient() {

        return new OkHttpTelegramClient(botToken);
    }
}

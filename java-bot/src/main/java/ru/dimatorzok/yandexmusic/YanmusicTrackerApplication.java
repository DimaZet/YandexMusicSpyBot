package ru.dimatorzok.yandexmusic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.dimatorzok.yandexmusic.external")
@EnableScheduling
public class YanmusicTrackerApplication {

    public static void main(String[] args) {

        SpringApplication.run(YanmusicTrackerApplication.class, args);
    }
}

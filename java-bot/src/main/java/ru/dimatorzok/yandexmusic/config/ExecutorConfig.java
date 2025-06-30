package ru.dimatorzok.yandexmusic.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorConfig {

    @Value("${executor.task.threads:5}")
    private Integer taskThreads;

    @Bean
    ExecutorService taskExecuror() {

        return Executors.newFixedThreadPool(taskThreads);
    }

}

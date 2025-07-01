package ru.dimatorzok.yandexmusic;

import java.util.concurrent.ExecutorService;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.shaded.com.google.common.util.concurrent.MoreExecutors;

@TestConfiguration
public class TestExecutorsConfig {

    @Bean
    @Primary
    ExecutorService singleThreadImmediate() {

        return MoreExecutors.newDirectExecutorService();
    }
}

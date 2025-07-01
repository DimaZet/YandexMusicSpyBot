package ru.dimatorzok.yandexmusic.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.dimatorzok.yandexmusic.entity.OutboxEvent;
import ru.dimatorzok.yandexmusic.repository.OutboxEventRepository;
import ru.dimatorzok.yandexmusic.service.AudioOutboxProcessingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventScheduler {

    private final OutboxEventRepository outboxEventRepository;

    private final AudioOutboxProcessingService audioOutboxProcessingService;

    @Scheduled(fixedDelay = 5000) // Каждые 5 секунд
    public void processOutboxEvents() {

        log.debug("Запуск обработки outbox событий");

        try {
            // Атомарно получаем события для обработки
            List<OutboxEvent> eventsToProcess = outboxEventRepository.markEventsAsProcessing();

            if (eventsToProcess.isEmpty()) {
                log.debug("Нет новых событий для обработки");
                return;
            }

            log.info("Найдено {} событий для обработки", eventsToProcess.size());

            // Обрабатываем каждое событие
            for (OutboxEvent event : eventsToProcess) {
                try {
                    if ("AUDIO_SEND".equals(event.getType())) {
                        audioOutboxProcessingService.processAudioEvent(event);
                    } else {
                        log.warn("Неизвестный тип события: {}", event.getType());
                    }
                } catch (Exception e) {
                    log.error("Ошибка при обработке события ID: {}", event.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("Ошибка в шедулере обработки outbox событий", e);
        }
    }
} 

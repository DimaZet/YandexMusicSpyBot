package ru.dimatorzok.yandexmusic.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.dimatorzok.yandexmusic.entity.OutboxEvent;
import ru.dimatorzok.yandexmusic.entity.OutboxEventStatus;
import ru.dimatorzok.yandexmusic.external.TrackInfo;
import ru.dimatorzok.yandexmusic.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    @Transactional
    public void createAudioSendEvent(Long chatId, String username, Long userId, List<TrackInfo> tracks) {

        try {
            AudioOutboxProcessingService.AudioEventPayload payload =
                    new AudioOutboxProcessingService.AudioEventPayload(chatId, username, userId, tracks);

            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEvent event = OutboxEvent.builder()
                    .type("AUDIO_SEND")
                    .payload(payloadJson)
                    .status(OutboxEventStatus.NEW)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();

            outboxEventRepository.save(event);
            log.info(
                    "Создано outbox событие для отправки аудио в чат: {}, пользователь: {}, треков: {}",
                    chatId,
                    username,
                    tracks.size()
            );

        } catch (JsonProcessingException e) {
            log.error("Ошибка при сериализации payload для создания outbox события", e);
            throw new RuntimeException("Не удалось создать outbox событие", e);
        }
    }
} 

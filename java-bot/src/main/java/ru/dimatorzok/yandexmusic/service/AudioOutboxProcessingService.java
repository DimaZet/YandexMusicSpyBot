package ru.dimatorzok.yandexmusic.service;

import java.util.List;

import org.springframework.stereotype.Service;

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
public class AudioOutboxProcessingService {

    private final OutboxEventRepository outboxEventRepository;

    private final AudioSenderService audioSenderService;

    private final ObjectMapper objectMapper;

    public void processAudioEvent(OutboxEvent event) {

        log.info("Начинаю обработку аудио события с ID: {}", event.getId());

        try {
            // Десериализуем payload
            AudioEventPayload payload = objectMapper.readValue(event.getPayload(), AudioEventPayload.class);

            log.info(
                    "Обрабатываю аудио событие для чата: {}, пользователя: {}, треков: {}",
                    payload.chatId(),
                    payload.username(),
                    payload.tracks().size()
            );

            // Получаем информацию о треках
            List<TrackInfo> tracks = payload.tracks();

            // Отправляем аудио
            audioSenderService.sendAudios(payload.chatId(), payload.username(), payload.userId(), tracks);

            // Помечаем событие как выполненное
            outboxEventRepository.updateStatus(event.getId(), OutboxEventStatus.DONE);
            log.info("Аудио событие с ID: {} успешно обработано", event.getId());

        } catch (JsonProcessingException e) {
            log.error("Ошибка при десериализации payload для события ID: {}", event.getId(), e);
            outboxEventRepository.updateStatus(event.getId(), OutboxEventStatus.ERROR);
        } catch (Exception e) {
            log.error("Ошибка при обработке аудио события ID: {}", event.getId(), e);
            outboxEventRepository.updateStatus(event.getId(), OutboxEventStatus.ERROR);
        }
    }

    public record AudioEventPayload(Long chatId, String username, Long userId, List<TrackInfo> tracks) {

    }
} 

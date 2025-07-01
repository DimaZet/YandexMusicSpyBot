package ru.dimatorzok.yandexmusic.scheduler;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.dimatorzok.yandexmusic.entity.Playlist;
import ru.dimatorzok.yandexmusic.external.PlaylistRequest;
import ru.dimatorzok.yandexmusic.external.TrackInfo;
import ru.dimatorzok.yandexmusic.repository.ChatRepository;
import ru.dimatorzok.yandexmusic.repository.SubscriptionRepository;
import ru.dimatorzok.yandexmusic.service.OutboxEventService;
import ru.dimatorzok.yandexmusic.service.YandexMusicService;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaylistScheduler {

    private final ExecutorService executorService;

    private final YandexMusicService yandexMusicService;

    private final SubscriptionRepository subscriptionRepository;

    private final ChatRepository chatRepository;

    private final OutboxEventService outboxEventService;

    @SneakyThrows
    @Scheduled(cron = "${telegram.bot.cron}")
    public void checkPlaylists() {

        log.info("start task");
        chatRepository.findAll().forEach(chat -> executorService.execute(() -> {
            log.info("start processing chat {}", chat.getId());
            subscriptionRepository.findByChat(chat).forEach(sub -> {
                var lastPlaylistUpdatedAt = Optional.ofNullable(sub.getPlaylistUpdatedAt()).orElse(OffsetDateTime.MIN);
                log.info("start processing subscription {}, lastUpdate {}", sub.getId(), lastPlaylistUpdatedAt);
                final Playlist playlist = sub.getPlaylist();
                var playlistResponse = yandexMusicService.fetchPlaylist(new PlaylistRequest(playlist.getPlaylistUrl()));
                List<TrackInfo> newTracks = playlistResponse.tracks()
                        .stream()
                        .takeWhile(trackInfo -> trackInfo.addedAt().isAfter(lastPlaylistUpdatedAt))
                        .limit(10)
                        .toList();
                if (!newTracks.isEmpty()) {
                    // Создаем outbox событие для отправки аудио
                    outboxEventService.createAudioSendEvent(
                            sub.getChat().getTelegramChatId(),
                            sub.getUser().getTelegramUsername(),
                            sub.getUser().getTelegramUserId(),
                            newTracks
                    );
                    sub.setPlaylistUpdatedAt(newTracks.getFirst().addedAt());
                    subscriptionRepository.save(sub);
                }
                log.info("processing subscription {} ended, {} new tracks", sub.getId(), newTracks.size());
            });
            log.info("stop processing chat {}", chat.getId());
        }));
    }
}

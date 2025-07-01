package ru.dimatorzok.yandexmusic.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import ru.dimatorzok.yandexmusic.AbstractIT;
import ru.dimatorzok.yandexmusic.entity.OutboxEvent;
import ru.dimatorzok.yandexmusic.entity.OutboxEventStatus;
import ru.dimatorzok.yandexmusic.external.PlaylistRequest;
import ru.dimatorzok.yandexmusic.external.PlaylistResponse;
import ru.dimatorzok.yandexmusic.external.TrackInfo;
import ru.dimatorzok.yandexmusic.service.AudioOutboxProcessingService;

class PlaylistSchedulerIT extends AbstractIT {

    @Test
    void checkPlaylists_createsOutboxEventIfNewTracks() {
        // arrange
        var lastUpdate = subscriptionRepository.findAll().getFirst().getPlaylistUpdatedAt();
        var tracks = Stream.of(1, 0, -1)
                .map(i -> new TrackInfo("trackId" + i, "trackTitle" + i, List.of("artist"), lastUpdate.plusDays(i)))
                .toList();
        var response = new PlaylistResponse(1L, "title", OffsetDateTime.MIN, OffsetDateTime.now(), tracks);
        when(yandexMusicService.fetchPlaylist(any(PlaylistRequest.class))).thenReturn(response);

        // act
        playlistScheduler.checkPlaylists();

        // assert
        var events = outboxEventRepository.findAll();
        var event = assertThat(events).singleElement();
        event.extracting(OutboxEvent::getType).isEqualTo("AUDIO_SEND");
        event.extracting(OutboxEvent::getStatus).isEqualTo(OutboxEventStatus.NEW);
        event.extracting(OutboxEvent::getPayload)
                .extracting(p -> {
                    try {
                        return objectMapper.readValue(p, AudioOutboxProcessingService.AudioEventPayload.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .usingRecursiveComparison()
                .isEqualTo(new AudioOutboxProcessingService.AudioEventPayload(100L,
                        "testuser",
                        1L,
                        tracks.subList(0, 1)
                ));
    }
} 

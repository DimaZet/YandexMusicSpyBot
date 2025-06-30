package ru.dimatorzok.yandexmusic.service;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ru.dimatorzok.yandexmusic.external.DownloadTrackRequest;
import ru.dimatorzok.yandexmusic.external.PlaylistRequest;
import ru.dimatorzok.yandexmusic.external.PlaylistResponse;
import ru.dimatorzok.yandexmusic.external.YandexMusicAdapterClient;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
@Service
public class YandexMusicService {

    private final YandexMusicAdapterClient yandexMusicAdapterClient;

    public PlaylistResponse fetchPlaylist(PlaylistRequest request) {

        return yandexMusicAdapterClient.fetchPlaylist(request);
    }

    @SneakyThrows
    public ByteArrayInputStream downloadTrackContent(DownloadTrackRequest request) {

        return new ByteArrayInputStream(yandexMusicAdapterClient.downloadTrackContent(request)
                .body()
                .asInputStream()
                .readAllBytes());
    }

    public Map<String, ByteArrayInputStream> downloadTrackContent(List<String> trackIds) {

        return trackIds.stream()
                .parallel()
                .collect(Collectors.toMap(
                        Function.identity(),
                        trackId -> downloadTrackContent(new DownloadTrackRequest(trackId))
                ));
    }

}

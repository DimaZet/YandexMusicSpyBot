package ru.dimatorzok.yandexmusic.external;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DownloadTrackRequest(
        @JsonProperty("track_id")
        String trackId
) { }

package ru.dimatorzok.yandexmusic.external;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TrackInfo(
        @JsonProperty("track_id")
        String trackId,
        @JsonProperty("title")
        String title,
        @JsonProperty("artists")
        List<String> artists,
        @JsonProperty("added_at")
        OffsetDateTime addedAt
) {}

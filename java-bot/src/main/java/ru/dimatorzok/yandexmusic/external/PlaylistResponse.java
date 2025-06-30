package ru.dimatorzok.yandexmusic.external;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlaylistResponse(
        @JsonProperty("playlist_id")
        Long playlistId,

        @JsonProperty("title")
        String title,

        @JsonProperty("created")
        OffsetDateTime created,

        @JsonProperty("modified")
        OffsetDateTime modified,

        @JsonProperty("tracks")
        List<TrackInfo> tracks
) { }


package ru.dimatorzok.yandexmusic.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import feign.Response;

@FeignClient(
        name = "yandexMusicAdapterClient",
        url = "${YANDEX_ADAPTER_BASE_URL:http://localhost:5000}"
)
public interface YandexMusicAdapterClient {

    @PostMapping("/fetch_playlist")
    PlaylistResponse fetchPlaylist(PlaylistRequest request);

    @PostMapping(value = "/download_track_content", consumes = "application/json")
    @ResponseBody
    Response downloadTrackContent(@RequestBody DownloadTrackRequest request);
}

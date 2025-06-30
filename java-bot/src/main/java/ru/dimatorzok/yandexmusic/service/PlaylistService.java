package ru.dimatorzok.yandexmusic.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import ru.dimatorzok.yandexmusic.entity.Playlist;
import ru.dimatorzok.yandexmusic.entity.User;
import ru.dimatorzok.yandexmusic.repository.PlaylistRepository;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    public PlaylistService(PlaylistRepository playlistRepository) {

        this.playlistRepository = playlistRepository;
    }

    public Playlist registerOrGetPlaylist(User user, String url) {

        return playlistRepository.findByPlaylistUrl(url).orElseGet(() -> {
            Playlist playlist = new Playlist();
            playlist.setUser(user);
            playlist.setPlaylistUrl(url);
            return playlistRepository.save(playlist);
        });
    }
}

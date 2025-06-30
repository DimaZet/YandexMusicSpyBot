
package ru.dimatorzok.yandexmusic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.dimatorzok.yandexmusic.entity.Playlist;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    Optional<Playlist> findByPlaylistUrl(String url);
}

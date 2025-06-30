
package ru.dimatorzok.yandexmusic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.dimatorzok.yandexmusic.entity.Chat;
import ru.dimatorzok.yandexmusic.entity.Playlist;
import ru.dimatorzok.yandexmusic.entity.Subscription;
import ru.dimatorzok.yandexmusic.entity.User;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserAndChatAndPlaylist(User user, Chat chat, Playlist playlist);

    List<Subscription> findByChat(Chat chat);
}


package ru.dimatorzok.yandexmusic.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import ru.dimatorzok.yandexmusic.entity.Chat;
import ru.dimatorzok.yandexmusic.entity.Playlist;
import ru.dimatorzok.yandexmusic.entity.Subscription;
import ru.dimatorzok.yandexmusic.entity.User;
import ru.dimatorzok.yandexmusic.repository.SubscriptionRepository;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription registerOrGetSubscription(User user, Chat chat, Playlist playlist) {
        return subscriptionRepository.findByUserAndChatAndPlaylist(user, chat, playlist)
                .orElseGet(() -> {
                    Subscription subscription = new Subscription();
                    subscription.setUser(user);
                    subscription.setChat(chat);
                    subscription.setPlaylist(playlist);
                    subscription.setPlaylistUpdatedAt(OffsetDateTime.now(ZoneId.of("Europe/Moscow")));
                    return subscriptionRepository.save(subscription);
                });
    }
}

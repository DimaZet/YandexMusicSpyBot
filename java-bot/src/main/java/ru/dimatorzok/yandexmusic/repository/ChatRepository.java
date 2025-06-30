package ru.dimatorzok.yandexmusic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.dimatorzok.yandexmusic.entity.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findByTelegramChatId(Long telegramChatId);
}

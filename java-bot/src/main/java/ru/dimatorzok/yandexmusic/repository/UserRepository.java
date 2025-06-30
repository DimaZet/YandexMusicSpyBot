
package ru.dimatorzok.yandexmusic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.dimatorzok.yandexmusic.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByTelegramUserId(Long telegramUserId);
}

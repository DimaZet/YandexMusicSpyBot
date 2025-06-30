
package ru.dimatorzok.yandexmusic.service;

import org.springframework.stereotype.Service;

import ru.dimatorzok.yandexmusic.entity.User;
import ru.dimatorzok.yandexmusic.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerOrGetUser(Long telegramUserId, String username) {
        return userRepository.findByTelegramUserId(telegramUserId)
                .orElseGet(() -> {
                    User user = new User();
                    user.setTelegramUserId(telegramUserId);
                    user.setTelegramUsername(username);
                    return userRepository.save(user);
                });
    }
}


package ru.dimatorzok.yandexmusic.service;

import org.springframework.stereotype.Service;

import ru.dimatorzok.yandexmusic.entity.Chat;
import ru.dimatorzok.yandexmusic.repository.ChatRepository;

@Service
public class ChatService {
    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Chat registerOrGetChat(Long telegramChatId, String title) {
        return chatRepository.findByTelegramChatId(telegramChatId)
                .map(chat -> {
                    chat.setTitle(title);
                    return chat;
                })
                .orElseGet(() -> {
                    Chat chat = new Chat();
                    chat.setTelegramChatId(telegramChatId);
                    chat.setTitle(title);
                    return chatRepository.save(chat);
                });
    }
}

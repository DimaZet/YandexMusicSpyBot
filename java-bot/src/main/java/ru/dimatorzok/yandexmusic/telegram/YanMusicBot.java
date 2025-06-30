package ru.dimatorzok.yandexmusic.telegram;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.reactions.SetMessageReaction;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import ru.dimatorzok.yandexmusic.entity.Chat;
import ru.dimatorzok.yandexmusic.entity.Playlist;
import ru.dimatorzok.yandexmusic.entity.User;
import ru.dimatorzok.yandexmusic.service.ChatService;
import ru.dimatorzok.yandexmusic.service.PlaylistService;
import ru.dimatorzok.yandexmusic.service.SubscriptionService;
import ru.dimatorzok.yandexmusic.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class YanMusicBot implements LongPollingUpdateConsumer {

    private final UserService userService;

    private final ChatService chatService;

    private final PlaylistService playlistService;

    private final SubscriptionService subscriptionService;

    private final TelegramClient telegramClient;

    private final ExecutorService executorService;

    @Override
    public void consume(final List<Update> updates) {

        updates.stream()
                .filter(Update::hasMessage)
                .map(Update::getMessage)
                .filter(Message::hasText)
                .forEach(update -> executorService.execute(() -> handleText(update)));
    }

    private void handleText(Message message) {

        String text = message.getText();
        if (text.startsWith("/subscribe ")) {
            String url = text.substring("/subscribe ".length()).trim();
            Long telegramUserId = message.getFrom().getId();
            String username = message.getFrom().getUserName();
            User user = userService.registerOrGetUser(telegramUserId, username);
            Chat chat = chatService.registerOrGetChat(
                    message.getChatId(),
                    message.getChat().getTitle() != null ? message.getChat().getTitle() : "Private chat"
            );
            Playlist playlist = playlistService.registerOrGetPlaylist(user, url);
            subscriptionService.registerOrGetSubscription(user, chat, playlist);
            try {
                telegramClient.execute(SetMessageReaction.builder()
                        .chatId(message.getChatId())
                        .messageId(message.getMessageId())
                        .reactionTypes(List.of(ReactionTypeEmoji.builder().emoji("🫡").build()))
                        .build());
            } catch (Exception e) {
                log.error(String.format("Ошибка при отправке сообщения в чат %s", message.getChatId()), e);
            }
        }
    }
}

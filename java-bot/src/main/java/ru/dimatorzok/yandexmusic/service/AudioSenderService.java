package ru.dimatorzok.yandexmusic.service;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaAudio;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import ru.dimatorzok.yandexmusic.external.TrackInfo;
import ru.dimatorzok.yandexmusic.telegram.MessageFormatter;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioSenderService {

    public static final String FOOTER_DELIMETER = "  \n\n";

    private final YandexMusicService yandexMusicService;

    private final TelegramClient telegramClient;

    public void sendAudios(Long chatId, String username, Long userId, List<TrackInfo> newTracks) {

        var loadedTracks = yandexMusicService.downloadTrackContent(newTracks.stream().map(TrackInfo::trackId).toList());
        final String textCaption = MessageFormatter.buildPlaylistUpdateMessage(username, userId);
        final List<InputMedia> medias = newTracks.stream().map(track -> {
            final String title = getTitle(track);
            return (InputMedia) (InputMediaAudio.builder()
                    .media(loadedTracks.get(track.trackId()), title)
                    .title(title)
                    .caption(getCaption(track))
                    .parseMode(ParseMode.MARKDOWNV2)
                    .build());
        }).toList();
        final InputMedia last = medias.getLast();
        last.setCaption(last.getCaption() + FOOTER_DELIMETER + textCaption);
        last.setParseMode(ParseMode.MARKDOWNV2);
        Runnable sendLogic = () -> {
            if (medias.size() == 1) {
                InputMediaAudio singleAudio = (InputMediaAudio) medias.getFirst();
                try {
                    telegramClient.execute(SendAudio.builder()
                            .chatId(chatId)
                            .audio(new InputFile(singleAudio.getNewMediaStream(), singleAudio.getMediaName()))
                            .title(singleAudio.getTitle())
                            .caption(getCaption(newTracks.getFirst()) + FOOTER_DELIMETER + textCaption)
                            .parseMode(ParseMode.MARKDOWNV2)
                            .disableNotification(true)
                            .build());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    telegramClient.execute(SendMediaGroup.builder()
                            .chatId(chatId)
                            .medias(medias)
                            .disableNotification(true)
                            .build());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(10)
                .waitDuration(Duration.ofSeconds(2))
                .retryOnException(e -> e.getMessage().contains("retry after"))
                .failAfterMaxAttempts(true)
                .build()).retry("sendAudio").executeRunnable(sendLogic);
    }

    private static String getCaption(final TrackInfo track) {

        var title = getTitle(track);
        return "🎵" + MessageFormatter.link(title, "https://music.yandex.ru/track/" + track.trackId());
    }

    private static String getTitle(final TrackInfo track) {

        return track.title() + " - " + String.join(", ", track.artists());
    }
} 

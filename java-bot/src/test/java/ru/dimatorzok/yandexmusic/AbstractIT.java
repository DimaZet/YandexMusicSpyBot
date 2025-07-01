package ru.dimatorzok.yandexmusic;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.dimatorzok.yandexmusic.entity.Chat;
import ru.dimatorzok.yandexmusic.entity.Playlist;
import ru.dimatorzok.yandexmusic.entity.Subscription;
import ru.dimatorzok.yandexmusic.entity.User;
import ru.dimatorzok.yandexmusic.repository.ChatRepository;
import ru.dimatorzok.yandexmusic.repository.OutboxEventRepository;
import ru.dimatorzok.yandexmusic.repository.PlaylistRepository;
import ru.dimatorzok.yandexmusic.repository.SubscriptionRepository;
import ru.dimatorzok.yandexmusic.repository.UserRepository;
import ru.dimatorzok.yandexmusic.scheduler.PlaylistScheduler;
import ru.dimatorzok.yandexmusic.service.YandexMusicService;

@Testcontainers
@SpringBootTest(classes = TestExecutorsConfig.class)
@ActiveProfiles("test")
public class AbstractIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    protected PlaylistScheduler playlistScheduler;

    @Autowired
    protected OutboxEventRepository outboxEventRepository;

    @MockBean
    protected YandexMusicService yandexMusicService;

    @Autowired
    protected ChatRepository chatRepository;

    @Autowired
    protected PlaylistRepository playlistRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected SubscriptionRepository subscriptionRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected TelegramClient telegramClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        // Подготовка данных
        User user = new User();
        user.setTelegramUserId(1L);
        user.setTelegramUsername("testuser");
        userRepository.save(user);
        Chat chat = new Chat();
        chat.setTelegramChatId(100L);
        chat.setTitle("Test chat");
        Playlist playlist = new Playlist();
        playlist.setPlaylistUrl("url");
        playlist.setUser(user);
        chatRepository.save(chat);
        playlistRepository.save(playlist);
        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setChat(chat);
        sub.setPlaylist(playlist);
        sub.setPlaylistUpdatedAt(OffsetDateTime.now().minus(Duration.ofDays(3)));
        subscriptionRepository.save(sub);
    }
}

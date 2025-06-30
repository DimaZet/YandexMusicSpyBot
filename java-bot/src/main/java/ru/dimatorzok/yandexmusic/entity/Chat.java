package ru.dimatorzok.yandexmusic.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "chat")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

    @Column(name = "title")
    private String title;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}

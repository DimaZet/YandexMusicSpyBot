package ru.dimatorzok.yandexmusic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.dimatorzok.yandexmusic.entity.OutboxEvent;
import ru.dimatorzok.yandexmusic.entity.OutboxEventStatus;

import jakarta.transaction.Transactional;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Modifying
    @Query(value = """
            UPDATE outbox_event
            SET status = 'PROCESSING', updated_at = NOW()
            WHERE id IN (
                SELECT id FROM outbox_event
                WHERE status = 'NEW'
                ORDER BY created_at
                LIMIT 10
                FOR UPDATE SKIP LOCKED
            )
            RETURNING *
            """, nativeQuery = true)
    List<OutboxEvent> markEventsAsProcessing();

    @Transactional
    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = :status, e.updatedAt = offset_datetime() WHERE e.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") OutboxEventStatus status);
} 

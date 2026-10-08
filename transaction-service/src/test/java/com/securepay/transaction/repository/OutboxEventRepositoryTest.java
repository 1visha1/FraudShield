package com.fraudshield.transaction.repository;

import com.fraudshield.transaction.entity.OutboxEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false"
})
class OutboxEventRepositoryTest {

    @Autowired
    private OutboxEventRepository repository;

    @Test
    void shouldReturnOnlyUnpublishedEvents() {

        repository.deleteAll();

        OutboxEvent first = OutboxEvent.builder()
                .aggregateType("TRANSACTION")
                .aggregateId(UUID.randomUUID())
                .eventType("CREATED")
                .payload("one")
                .published(false)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();

        OutboxEvent second = OutboxEvent.builder()
                .aggregateType("TRANSACTION")
                .aggregateId(UUID.randomUUID())
                .eventType("APPROVED")
                .payload("two")
                .published(false)
                .createdAt(LocalDateTime.now())
                .build();

        OutboxEvent published = OutboxEvent.builder()
                .aggregateType("TRANSACTION")
                .aggregateId(UUID.randomUUID())
                .eventType("BLOCKED")
                .payload("three")
                .published(true)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(first);
        repository.save(second);
        repository.save(published);

        List<OutboxEvent> result =
                repository.findByPublishedFalseOrderByCreatedAtAsc();

        assertEquals(2, result.size());
        assertEquals("one", result.get(0).getPayload());
        assertEquals("two", result.get(1).getPayload());
    }

    @Test
    void shouldReturnEmptyList() {

        repository.deleteAll();

        List<OutboxEvent> result =
                repository.findByPublishedFalseOrderByCreatedAtAsc();

        assertTrue(result.isEmpty());
    }
}
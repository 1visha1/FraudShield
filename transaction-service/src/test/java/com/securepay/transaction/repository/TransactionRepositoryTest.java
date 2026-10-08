package com.fraudshield.transaction.repository;

import com.fraudshield.transaction.entity.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repository;

    @Test
    @DisplayName("Should save transaction successfully")
    void shouldSaveTransaction() {

        Transaction transaction = Transaction.builder()
                .customerId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(1000))
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        Transaction saved = repository.save(transaction);

        assertNotNull(saved.getId());
        assertEquals("PENDING", saved.getStatus());
        assertEquals(BigDecimal.valueOf(1000), saved.getAmount());
    }

    @Test
    @DisplayName("Should find transaction by id")
    void shouldFindTransactionById() {

        Transaction transaction = Transaction.builder()
                .customerId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(250))
                .status("APPROVED")
                .createdAt(LocalDateTime.now())
                .build();

        Transaction saved = repository.save(transaction);

        Optional<Transaction> result = repository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
        assertEquals("APPROVED", result.get().getStatus());
    }

    @Test
    @DisplayName("Should update transaction")
    void shouldUpdateTransaction() {

        Transaction transaction = Transaction.builder()
                .customerId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(500))
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        Transaction saved = repository.save(transaction);

        saved.setStatus("APPROVED");

        repository.save(saved);

        Transaction updated = repository.findById(saved.getId()).orElseThrow();

        assertEquals("APPROVED", updated.getStatus());
    }

    @Test
    @DisplayName("Should delete transaction")
    void shouldDeleteTransaction() {

        Transaction transaction = Transaction.builder()
                .customerId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(750))
                .status("BLOCK")
                .createdAt(LocalDateTime.now())
                .build();

        Transaction saved = repository.save(transaction);

        repository.deleteById(saved.getId());

        assertFalse(repository.findById(saved.getId()).isPresent());
    }

    @Test
    @DisplayName("Should return empty optional when transaction does not exist")
    void shouldReturnEmptyWhenNotFound() {

        Optional<Transaction> result =
                repository.findById(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }
}
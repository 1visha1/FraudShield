package com.fraudshield.auth.repository;

import com.fraudshield.auth.entity.AuthSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AuthSessionRepositoryTest {

    @Autowired
    private AuthSessionRepository repository;

    @Test
    void findByTransactionId_ShouldReturnSession_WhenTransactionExists() {

        UUID transactionId = UUID.randomUUID();

        AuthSession session = AuthSession.builder()
                .transactionId(transactionId)
                .customerId(UUID.randomUUID())
                .authType("SMS_OTP")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        repository.saveAndFlush(session);

        Optional<AuthSession> result =
                repository.findByTransactionId(transactionId);

        assertTrue(result.isPresent());
        assertEquals(transactionId, result.get().getTransactionId());
        assertEquals("SMS_OTP", result.get().getAuthType());
        assertEquals("PENDING", result.get().getStatus());
    }

    @Test
    void findByTransactionId_ShouldReturnEmpty_WhenTransactionDoesNotExist() {

        Optional<AuthSession> result =
                repository.findByTransactionId(UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    void save_ShouldPersistEntity() {

        AuthSession session = AuthSession.builder()
                .transactionId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .authType("SMS_EMAIL_OTP")
                .status("VERIFIED")
                .createdAt(LocalDateTime.now())
                .build();

        AuthSession saved = repository.saveAndFlush(session);

        assertNotNull(saved.getId());
        assertEquals("SMS_EMAIL_OTP", saved.getAuthType());
        assertEquals("VERIFIED", saved.getStatus());
    }

    @Test
    void update_ShouldUpdateStatus() {

        AuthSession session = AuthSession.builder()
                .transactionId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .authType("SMS_OTP")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        AuthSession saved = repository.saveAndFlush(session);

        saved.setStatus("COMPLETED");

        repository.saveAndFlush(saved);

        Optional<AuthSession> updated =
                repository.findById(saved.getId());

        assertTrue(updated.isPresent());
        assertEquals("COMPLETED", updated.get().getStatus());
    }

    @Test
    void delete_ShouldRemoveEntity() {

        AuthSession session = AuthSession.builder()
                .transactionId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .authType("SMS_OTP")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        AuthSession saved = repository.saveAndFlush(session);

        repository.delete(saved);
        repository.flush();

        Optional<AuthSession> result =
                repository.findById(saved.getId());

        assertFalse(result.isPresent());
    }
}
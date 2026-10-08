package com.fraudshield.fraud.repository;

import com.fraudshield.fraud.entity.FraudDecision;
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
class FraudDecisionRepositoryTest {

    @Autowired
    private FraudDecisionRepository repository;

    @Test
    void shouldSaveFraudDecision() {

        FraudDecision decision = FraudDecision.builder()
                .transactionId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .fraudScore(85)
                .decision("HIGH_RISK")
                .createdAt(LocalDateTime.now())
                .build();

        FraudDecision saved = repository.save(decision);

        assertNotNull(saved.getId());
        assertEquals(85, saved.getFraudScore());
        assertEquals("HIGH_RISK", saved.getDecision());
    }

    @Test
    void shouldFindFraudDecisionById() {

        FraudDecision decision = FraudDecision.builder()
                .transactionId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .fraudScore(40)
                .decision("STEP_UP_AUTH")
                .createdAt(LocalDateTime.now())
                .build();

        FraudDecision saved = repository.save(decision);

        Optional<FraudDecision> result = repository.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenIdDoesNotExist() {

        Optional<FraudDecision> result =
                repository.findById(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteFraudDecision() {

        FraudDecision decision = FraudDecision.builder()
                .transactionId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .fraudScore(20)
                .decision("APPROVE")
                .createdAt(LocalDateTime.now())
                .build();

        FraudDecision saved = repository.save(decision);

        repository.delete(saved);

        assertFalse(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void shouldReturnAllFraudDecisions() {

        repository.save(
                FraudDecision.builder()
                        .transactionId(UUID.randomUUID())
                        .customerId(UUID.randomUUID())
                        .fraudScore(10)
                        .decision("APPROVE")
                        .createdAt(LocalDateTime.now())
                        .build());

        repository.save(
                FraudDecision.builder()
                        .transactionId(UUID.randomUUID())
                        .customerId(UUID.randomUUID())
                        .fraudScore(90)
                        .decision("HIGH_RISK")
                        .createdAt(LocalDateTime.now())
                        .build());

        assertEquals(2, repository.findAll().size());
    }
}
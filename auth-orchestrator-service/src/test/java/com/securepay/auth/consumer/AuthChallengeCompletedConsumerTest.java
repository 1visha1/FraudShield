package com.fraudshield.auth.consumer;

import com.fraudshield.auth.event.AuthChallengeCompletedEvent;
import com.fraudshield.auth.service.AuthOrchestratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthChallengeCompletedConsumerTest {

    @Mock
    private AuthOrchestratorService authOrchestratorService;

    @InjectMocks
    private AuthChallengeCompletedConsumer consumer;

    private AuthChallengeCompletedEvent event;

    @BeforeEach
    void setUp() {
        event = AuthChallengeCompletedEvent.builder()
                .transactionId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .authSessionId(UUID.randomUUID())
                .verified(true)
                .status("COMPLETED")
                .build();
    }

    @Test
    void consume_ShouldCallHandleChallengeCompletion() {

        consumer.consume(event);

        verify(authOrchestratorService, times(1))
                .handleChallengeCompletion(
                        event.getTransactionId(),
                        "COMPLETED");

        verifyNoMoreInteractions(authOrchestratorService);
    }

    @Test
    void consume_ShouldHandleFailedStatus() {

        event.setStatus("FAILED");

        consumer.consume(event);

        verify(authOrchestratorService, times(1))
                .handleChallengeCompletion(
                        event.getTransactionId(),
                        "FAILED");

        verifyNoMoreInteractions(authOrchestratorService);
    }

    @Test
    void consume_ShouldHandleNullStatus() {

        event.setStatus(null);

        consumer.consume(event);

        verify(authOrchestratorService, times(1))
                .handleChallengeCompletion(
                        event.getTransactionId(),
                        null);

        verifyNoMoreInteractions(authOrchestratorService);
    }

    @Test
    void consume_ShouldHandleDifferentTransactionId() {

        UUID transactionId = UUID.randomUUID();

        event.setTransactionId(transactionId);
        event.setStatus("COMPLETED");

        consumer.consume(event);

        verify(authOrchestratorService, times(1))
                .handleChallengeCompletion(
                        transactionId,
                        "COMPLETED");

        verifyNoMoreInteractions(authOrchestratorService);
    }
}
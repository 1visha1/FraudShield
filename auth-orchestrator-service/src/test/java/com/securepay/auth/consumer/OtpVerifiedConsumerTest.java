package com.securepay.auth.consumer;

import com.securepay.auth.event.OtpVerifiedEvent;
import com.securepay.auth.service.AuthOrchestratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpVerifiedConsumerTest {

    @Mock
    private AuthOrchestratorService authOrchestratorService;

    @InjectMocks
    private OtpVerifiedConsumer otpVerifiedConsumer;

    private OtpVerifiedEvent event;

    @BeforeEach
    void setUp() {
        event = new OtpVerifiedEvent();
        event.setTransactionId(UUID.randomUUID());
    }

    @Test
    void consume_ShouldInvokeVerifyChallenge() {

        otpVerifiedConsumer.consume(event);

        verify(authOrchestratorService, times(1))
                .verifyChallenge(event.getTransactionId());

        verifyNoMoreInteractions(authOrchestratorService);
    }

    @Test
    void consume_ShouldHandleDifferentTransactionId() {

        UUID transactionId = UUID.randomUUID();

        event.setTransactionId(transactionId);

        otpVerifiedConsumer.consume(event);

        verify(authOrchestratorService)
                .verifyChallenge(transactionId);

        verifyNoMoreInteractions(authOrchestratorService);
    }

    @Test
    void consume_ShouldHandleNullTransactionId() {

        event.setTransactionId(null);

        otpVerifiedConsumer.consume(event);

        verify(authOrchestratorService)
                .verifyChallenge(null);

        verifyNoMoreInteractions(authOrchestratorService);
    }

    @Test
    void consume_ShouldInvokeVerifyChallengeOnlyOnce() {

        otpVerifiedConsumer.consume(event);

        verify(authOrchestratorService, only())
                .verifyChallenge(event.getTransactionId());
    }
}
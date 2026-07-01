package com.securepay.auth.service;

import com.securepay.auth.dto.AuthType;
import com.securepay.auth.entity.AuthSession;
import com.securepay.auth.event.AuthChallengeCreatedEvent;
import com.securepay.auth.event.FraudDetectedEvent;
import com.securepay.auth.publisher.AuthChallengePublisher;
import com.securepay.auth.repository.AuthSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthOrchestratorServiceTest {

    @Mock
    private AuthDecisionService decisionService;

    @Mock
    private AuthSessionRepository repository;

    @Mock
    private AuthChallengePublisher publisher;

    @Mock
    private AuthSessionCache cache;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthOrchestratorService service;

    private FraudDetectedEvent event;

    @BeforeEach
    void setUp() {

        event = new FraudDetectedEvent();
        event.setTransactionId(UUID.randomUUID());
        event.setCustomerId(UUID.randomUUID());
        event.setFraudScore(60);
        event.setDecision("STEP_UP_AUTH");
    }

    @Test
    void process_ShouldApproveTransaction() {

        event.setDecision("APPROVE");

        service.process(event);

        verify(rabbitTemplate, times(1))
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(Object.class));

        verify(repository, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void process_ShouldBlockTransaction() {

        event.setDecision("BLOCK");

        service.process(event);

        verify(rabbitTemplate, times(1))
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(Object.class));

        verify(repository, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void process_ShouldCreateSmsOtpChallenge() {

        when(decisionService.determine(anyString(), anyInt()))
                .thenReturn(AuthType.SMS_OTP);

        service.process(event);

        verify(repository).save(any(AuthSession.class));

        verify(cache).save(
                event.getTransactionId(),
                "SMS_OTP"
        );

        verify(publisher).publish(any(AuthChallengeCreatedEvent.class));
    }

    @Test
    void process_ShouldCreateSmsEmailOtpChallenge() {

        event.setDecision("HIGH_RISK");

        when(decisionService.determine(anyString(), anyInt()))
                .thenReturn(AuthType.SMS_EMAIL_OTP);

        service.process(event);

        verify(repository).save(any(AuthSession.class));

        verify(cache).save(
                event.getTransactionId(),
                "SMS_EMAIL_OTP"
        );

        verify(publisher).publish(any(AuthChallengeCreatedEvent.class));
    }

    @Test
    void process_ShouldApprove_WhenDecisionServiceReturnsNone() {

        when(decisionService.determine(anyString(), anyInt()))
                .thenReturn(AuthType.NONE);

        service.process(event);

        verify(rabbitTemplate)
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(Object.class));
    }

    @Test
    void process_ShouldHandleRepositoryException() {

        when(decisionService.determine(anyString(), anyInt()))
                .thenReturn(AuthType.SMS_OTP);

        doThrow(new RuntimeException("Database Error"))
                .when(repository)
                .save(any(AuthSession.class));

        assertDoesNotThrow(() -> service.process(event));

        verify(repository).save(any(AuthSession.class));

        verify(publisher, never()).publish(any());
    }

    @Test
    void process_ShouldPopulateChallengeEventCorrectly() {

        when(decisionService.determine(anyString(), anyInt()))
                .thenReturn(AuthType.SMS_OTP);

        service.process(event);

        ArgumentCaptor<AuthChallengeCreatedEvent> captor =
                ArgumentCaptor.forClass(AuthChallengeCreatedEvent.class);

        verify(publisher).publish(captor.capture());

        AuthChallengeCreatedEvent published = captor.getValue();

        assertEquals(event.getTransactionId(), published.getTransactionId());
        assertEquals(event.getCustomerId(), published.getCustomerId());
        assertEquals("SMS_OTP", published.getAuthType());
        assertEquals("PENDING", published.getStatus());
    }
    @Test
    void verifyChallenge_ShouldVerifySession() {

        UUID transactionId = UUID.randomUUID();

        AuthSession session = AuthSession.builder()
                .id(UUID.randomUUID())
                .transactionId(transactionId)
                .customerId(UUID.randomUUID())
                .status("PENDING")
                .authType("SMS_OTP")
                .build();

        when(repository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(session));

        service.verifyChallenge(transactionId);

        assertEquals("VERIFIED", session.getStatus());

        verify(repository).save(session);

        verify(rabbitTemplate)
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(Object.class));
    }

    @Test
    void verifyChallenge_ShouldDoNothing_WhenSessionNotFound() {

        UUID transactionId = UUID.randomUUID();

        when(repository.findByTransactionId(transactionId))
                .thenReturn(Optional.empty());

        service.verifyChallenge(transactionId);

        verify(repository, never()).save(any());

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void handleChallengeCompletion_ShouldCompleteChallenge() {

        UUID transactionId = UUID.randomUUID();

        AuthSession session = AuthSession.builder()
                .id(UUID.randomUUID())
                .transactionId(transactionId)
                .customerId(UUID.randomUUID())
                .status("PENDING")
                .authType("SMS_OTP")
                .build();

        when(repository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(session));

        service.handleChallengeCompletion(transactionId, "COMPLETED");

        assertEquals("COMPLETED", session.getStatus());

        verify(repository).save(session);

        verify(rabbitTemplate)
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(Object.class));
    }

    @Test
    void handleChallengeCompletion_ShouldFailChallenge() {

        UUID transactionId = UUID.randomUUID();

        AuthSession session = AuthSession.builder()
                .id(UUID.randomUUID())
                .transactionId(transactionId)
                .customerId(UUID.randomUUID())
                .status("PENDING")
                .authType("SMS_OTP")
                .build();

        when(repository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(session));

        service.handleChallengeCompletion(transactionId, "FAILED");

        assertEquals("FAILED", session.getStatus());

        verify(repository).save(session);

        verify(rabbitTemplate)
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(Object.class));
    }

    @Test
    void handleChallengeCompletion_ShouldBlock_WhenStatusIsNotCompleted() {

        UUID transactionId = UUID.randomUUID();

        AuthSession session = AuthSession.builder()
                .id(UUID.randomUUID())
                .transactionId(transactionId)
                .customerId(UUID.randomUUID())
                .status("PENDING")
                .authType("SMS_OTP")
                .build();

        when(repository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(session));

        service.handleChallengeCompletion(transactionId, "TIMEOUT");

        assertEquals("FAILED", session.getStatus());

        verify(repository).save(session);

        verify(rabbitTemplate)
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(Object.class));
    }

    @Test
    void handleChallengeCompletion_ShouldDoNothing_WhenSessionMissing() {

        UUID transactionId = UUID.randomUUID();

        when(repository.findByTransactionId(transactionId))
                .thenReturn(Optional.empty());

        service.handleChallengeCompletion(transactionId, "COMPLETED");

        verify(repository, never()).save(any());

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void process_ShouldSaveCorrectAuthTypeToCache() {

        event.setDecision("HIGH_RISK");

        when(decisionService.determine(anyString(), anyInt()))
                .thenReturn(AuthType.SMS_EMAIL_OTP);

        service.process(event);

        verify(cache).save(
                event.getTransactionId(),
                "SMS_EMAIL_OTP");
    }

    @Test
    void process_ShouldPublishChallengeEventOnce() {

        when(decisionService.determine(anyString(), anyInt()))
                .thenReturn(AuthType.SMS_OTP);

        service.process(event);

        verify(publisher, times(1))
                .publish(any(AuthChallengeCreatedEvent.class));
    }

    @Test
    void process_ShouldPersistSessionOnce() {

        when(decisionService.determine(anyString(), anyInt()))
                .thenReturn(AuthType.SMS_OTP);

        service.process(event);

        verify(repository, times(1))
                .save(any(AuthSession.class));
    }

}
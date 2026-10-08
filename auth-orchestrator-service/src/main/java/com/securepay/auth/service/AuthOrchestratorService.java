package com.fraudshield.auth.service;

import com.fraudshield.auth.dto.AuthType;
import com.fraudshield.auth.entity.AuthSession;
import com.fraudshield.auth.event.AuthChallengeCompletedEvent;
import com.fraudshield.auth.event.AuthChallengeCreatedEvent;
import com.fraudshield.auth.event.FraudDetectedEvent;
import com.fraudshield.auth.event.TransactionBlockedEvent;
import com.fraudshield.auth.publisher.AuthChallengePublisher;
import com.fraudshield.auth.repository.AuthSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthOrchestratorService {

    private final AuthDecisionService decisionService;
    private final AuthSessionRepository repository;
    private final AuthChallengePublisher publisher;
    private final AuthSessionCache cache;
    private final RabbitTemplate rabbitTemplate;

    public void process(FraudDetectedEvent event) {
        log.info("Processing FraudDetectedEvent. Decision: {}, Score: {}", event.getDecision(), event.getFraudScore());

        // TERMINAL DECISION: APPROVE
        if ("APPROVE".equals(event.getDecision())) {
            log.info("Automatic APPROVE for transaction: {}", event.getTransactionId());
            publishCompletion(event.getTransactionId(), event.getCustomerId(), null);
            return;
        }

        // TERMINAL DECISION: BLOCK
        if ("BLOCK".equals(event.getDecision())) {
            log.info("Automatic BLOCK for transaction: {}", event.getTransactionId());
            publishBlock(event.getTransactionId(), event.getCustomerId(), "Fraud score exceeds block threshold");
            return;
        }

        // CHALLENGE DECISIONS: STEP_UP_AUTH or HIGH_RISK
        AuthType authType = decisionService.determine(event.getDecision(), event.getFraudScore());
        
        if (authType == AuthType.NONE) {
            log.warn("Unknown decision type received: {}. Defaulting to APPROVE.", event.getDecision());
            publishCompletion(event.getTransactionId(), event.getCustomerId(), null);
            return;
        }

        log.info("Starting Multi-Factor Authentication: {} for txn: {}", authType, event.getTransactionId());
        initiateChallenge(event, authType);
    }

    private void initiateChallenge(FraudDetectedEvent event, AuthType authType) {
        try {
            AuthSession session = AuthSession.builder()
                    .transactionId(event.getTransactionId())
                    .customerId(event.getCustomerId())
                    .authType(authType.name())
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .build();

            repository.save(session);
            cache.save(event.getTransactionId(), authType.name());

            AuthChallengeCreatedEvent challengeEvent = AuthChallengeCreatedEvent.builder()
                    .transactionId(event.getTransactionId())
                    .customerId(event.getCustomerId())
                    .authType(authType.name())
                    .status("PENDING")
                    .build();

            publisher.publish(challengeEvent);
            log.info("MFA Challenge created and published: {}", authType);

        } catch (Exception e) {
            log.error("Failed to initiate auth challenge", e);
        }
    }

    @Transactional
    public void verifyChallenge(UUID transactionId) {
        repository.findByTransactionId(transactionId).ifPresentOrElse(session -> {
            session.setStatus("VERIFIED");
            repository.save(session);
            log.info("MFA Challenge VERIFIED for txn: {}", transactionId);
            publishCompletion(session.getTransactionId(), session.getCustomerId(), session.getId());
        }, () -> log.error("AuthSession not found for transaction: {}", transactionId));
    }

    @Transactional
    public void handleChallengeCompletion(UUID transactionId, String status) {
        repository.findByTransactionId(transactionId).ifPresentOrElse(session -> {
            if ("COMPLETED".equals(status)) {
                session.setStatus("COMPLETED");
                repository.save(session);
                log.info("MFA Challenge COMPLETED for txn: {}", transactionId);
                publishCompletion(session.getTransactionId(), session.getCustomerId(), session.getId());
            } else {
                session.setStatus("FAILED");
                repository.save(session);
                log.warn("MFA Challenge FAILED for txn: {}", transactionId);
                publishBlock(session.getTransactionId(), session.getCustomerId(), "MFA challenge failed");
            }
        }, () -> log.error("AuthSession not found for transaction: {}", transactionId));
    }

    private void publishCompletion(UUID transactionId, UUID customerId, UUID authSessionId) {
        AuthChallengeCompletedEvent event = AuthChallengeCompletedEvent.builder()
                .transactionId(transactionId)
                .customerId(customerId)
                .authSessionId(authSessionId)
                .verified(true)
                .verifiedAt(LocalDateTime.now())
                .build();
        
        rabbitTemplate.convertAndSend("fraudshield.exchange", "auth.challenge.completed", event);
    }

    private void publishBlock(UUID transactionId, UUID customerId, String reason) {
        TransactionBlockedEvent event = TransactionBlockedEvent.builder()
                .transactionId(transactionId)
                .customerId(customerId)
                .reason(reason)
                .build();

        rabbitTemplate.convertAndSend("fraudshield.exchange", "transaction.blocked", event);
    }
}
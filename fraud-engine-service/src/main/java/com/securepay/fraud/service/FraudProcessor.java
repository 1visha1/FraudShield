package com.fraudshield.fraud.service;

import com.fraudshield.fraud.cache.FraudContextCache;
import com.fraudshield.fraud.entity.FraudDecision;
import com.fraudshield.fraud.event.FraudDetectedEvent;
import com.fraudshield.fraud.publisher.FraudPublisher;
import com.fraudshield.fraud.repository.FraudDecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudProcessor {

    private final FraudContextCache cache;
    private final FraudScoringService scoringService;
    private final FraudPublisher publisher;
    private final FraudDecisionRepository repository;

    public void process(UUID transactionId) {
        try {
            log.info("Process start for txn: {}", transactionId);

            if (!cache.ready(transactionId)) {
                return;
            }

            Integer riskScore = cache.risk(transactionId);
            Integer ruleScore = cache.rule(transactionId);
            UUID customerId = cache.customerId(transactionId);

            Integer fraudScore = scoringService.calculate(riskScore, ruleScore);
            String decision = scoringService.decision(fraudScore);

            FraudDecision saved = repository.save(
                    FraudDecision.builder()
                            .transactionId(transactionId)
                            .customerId(customerId)
                            .fraudScore(fraudScore)
                            .decision(decision)
                            .createdAt(LocalDateTime.now())
                            .build());

            log.info("Fraud Decision saved to DB: {}", saved.getId());

            FraudDetectedEvent event = FraudDetectedEvent.builder()
                    .transactionId(transactionId)
                    .customerId(customerId != null ? customerId.toString() : null)
                    .fraudScore(fraudScore)
                    .decision(decision)
                    .build();

            log.info("Publishing FraudDetectedEvent: {}", event);
            publisher.publish(event);

        } catch (Exception e) {
            log.error("CRITICAL ERROR in FraudProcessor for txn {}: {}", transactionId, e.getMessage(), e);
        }
    }
}
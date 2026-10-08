package com.fraudshield.risk.consumer;

import com.fraudshield.risk.entity.RiskAssessment;
import com.fraudshield.risk.event.RiskAssessedEvent;
import com.fraudshield.risk.event.TransactionCreatedEvent;
import com.fraudshield.risk.publisher.RiskEventPublisher;
import com.fraudshield.risk.repository.RiskAssessmentRepository;
import com.fraudshield.risk.service.DeviceRiskCache;
import com.fraudshield.risk.service.RiskCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.fraudshield.risk.config.RabbitMQConfig;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TransactionConsumer {

    private final RiskCalculationService service;

    private final RiskAssessmentRepository repository;

    private final RiskEventPublisher publisher;

    private final DeviceRiskCache deviceRiskCache;

    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_CREATED_QUEUE)
    public void consume(TransactionCreatedEvent event) {
        java.util.UUID customerId = event.getCustomerId() != null ? java.util.UUID.fromString(event.getCustomerId()) : null;

        Integer deviceRisk =
                deviceRiskCache.get(customerId);

        Integer score =
                service.calculateRisk(
                        event.getAmount(),
                        customerId);

        String level =
                service.riskLevel(score);

        RiskAssessment assessment =
                RiskAssessment.builder()
                        .transactionId(event.getTransactionId())
                        .customerId(customerId)
                        .riskScore(score)
                        .riskLevel(level)
                        .assessedAt(LocalDateTime.now())
                        .build();

        repository.save(assessment);

        RiskAssessedEvent riskEvent =
                RiskAssessedEvent.builder()
                        .transactionId(event.getTransactionId())
                        .customerId(event.getCustomerId())
                        .amount(event.getAmount())
                        .riskScore(score)
                        .build();

        publisher.publish(riskEvent);
    }
}
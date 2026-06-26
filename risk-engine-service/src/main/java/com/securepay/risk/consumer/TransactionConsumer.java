package com.securepay.risk.consumer;

import com.securepay.risk.entity.RiskAssessment;
import com.securepay.risk.event.RiskAssessedEvent;
import com.securepay.risk.event.TransactionCreatedEvent;
import com.securepay.risk.publisher.RiskEventPublisher;
import com.securepay.risk.repository.RiskAssessmentRepository;
import com.securepay.risk.service.DeviceRiskCache;
import com.securepay.risk.service.RiskCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TransactionConsumer {

    private final RiskCalculationService service;

    private final RiskAssessmentRepository repository;

    private final RiskEventPublisher publisher;

    private final DeviceRiskCache deviceRiskCache;

    @RabbitListener(queues = "transaction.created.q")
    public void consume(TransactionCreatedEvent event) {

        Integer deviceRisk =
                deviceRiskCache.get(event.getCustomerId());

        Integer score =
                service.calculateRisk(
                        event.getAmount(),
                        event.getCustomerId());

        String level =
                service.riskLevel(score);

        RiskAssessment assessment =
                RiskAssessment.builder()
                        .transactionId(event.getTransactionId())
                        .customerId(event.getCustomerId())
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
                        .riskLevel(level)

                        // Temporary value
                        .deviceTrusted(false)

                        .deviceRiskScore(deviceRisk)
                        .build();

        publisher.publish(riskEvent);
    }
}
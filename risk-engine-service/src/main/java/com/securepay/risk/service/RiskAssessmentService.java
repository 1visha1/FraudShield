package com.fraudshield.risk.service;

import com.fraudshield.risk.config.RabbitMQConfig;
import com.fraudshield.risk.event.RiskAssessedEvent;
import com.fraudshield.risk.event.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentService {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_CREATED_QUEUE)
    public void assessRisk(TransactionCreatedEvent event) {
        log.info("Assessing risk for transaction: {}", event.getTransactionId());

        // Simple risk assessment logic for POC
        int riskScore = (int) (Math.random() * 100);

        RiskAssessedEvent riskAssessedEvent = RiskAssessedEvent.builder()
                .transactionId(event.getTransactionId())
                .customerId(event.getCustomerId())
                .amount(event.getAmount())
                .riskScore(riskScore)
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RISK_ASSESSED_KEY, riskAssessedEvent);
        log.info("Published RiskAssessedEvent for transaction: {}", event.getTransactionId());
    }
}
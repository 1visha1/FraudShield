package com.securepay.fraud.consumer;

import com.securepay.fraud.cache.FraudContextCache;
import com.securepay.fraud.config.RabbitMQConfig;
import com.securepay.fraud.event.RiskAssessedEvent;
import com.securepay.fraud.service.FraudProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RiskConsumer {

    private final FraudContextCache cache;
    private final FraudProcessor processor;

    @RabbitListener(queues = RabbitMQConfig.RISK_ASSESSED_QUEUE)
    public void consume(RiskAssessedEvent event) {
        log.info("Received RiskAssessedEvent: {}", event);
        cache.saveRisk(
                event.getTransactionId(),
                event.getCustomerId(),
                event.getRiskScore());

        processor.process(event.getTransactionId());
    }
}
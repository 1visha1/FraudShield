package com.securepay.fraud.consumer;

import com.securepay.fraud.cache.FraudContextCache;
import com.securepay.fraud.config.RabbitMQConfig;
import com.securepay.fraud.event.RuleEvaluatedEvent;
import com.securepay.fraud.service.FraudProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RuleConsumer {

    private final FraudContextCache cache;
    private final FraudProcessor processor;

    @RabbitListener(queues = RabbitMQConfig.RULE_EVALUATED_QUEUE)
    public void consume(RuleEvaluatedEvent event) {
        log.info("Received RuleEvaluatedEvent: {}", event);
        cache.saveRule(
                event.getTransactionId(),
                event.getCustomerId() != null ? UUID.fromString(event.getCustomerId()) : null,
                event.getRuleScore(),
                java.util.Collections.emptyList());

        processor.process(event.getTransactionId());
    }
}
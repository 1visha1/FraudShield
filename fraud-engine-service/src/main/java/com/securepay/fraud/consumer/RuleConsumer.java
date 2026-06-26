package com.securepay.fraud.consumer;

import com.securepay.fraud.cache.FraudContextCache;
import com.securepay.fraud.config.RabbitMQConfig;
import com.securepay.fraud.event.RuleEvaluatedEvent;
import com.securepay.fraud.service.FraudProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RuleConsumer {

    private final FraudContextCache cache;
    private final FraudProcessor processor;

    @RabbitListener(queues = RabbitMQConfig.RULE_QUEUE)
    public void consume(RuleEvaluatedEvent event) {
        log.info("Received RuleEvaluatedEvent: {}", event);
        cache.saveRule(
                event.getTransactionId(),
                event.getCustomerId(),
                event.getRuleScore(),
                event.getMatchedRules());

        processor.process(event.getTransactionId());
    }
}
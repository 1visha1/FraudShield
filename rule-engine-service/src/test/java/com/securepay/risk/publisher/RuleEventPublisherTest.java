package com.fraudshield.risk.publisher;

import com.fraudshield.risk.event.RuleEvaluatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RuleEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RuleEventPublisher publisher;

    @Test
    void publish_ShouldSendRuleEvaluatedEvent() {

        RuleEvaluatedEvent event = RuleEvaluatedEvent.builder()
                .transactionId(UUID.randomUUID())
                .customerId("CUS001")
                .amount(BigDecimal.valueOf(1500))
                .riskScore(70)
                .ruleScore(40)
                .build();

        publisher.publish(event);

        verify(rabbitTemplate, times(1))
                .convertAndSend(
                        "fraudshield.exchange",
                        "rule.evaluated",
                        event
                );
    }
}
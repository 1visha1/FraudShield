package com.fraudshield.fraud.consumer;

import com.fraudshield.fraud.cache.FraudContextCache;
import com.fraudshield.fraud.event.RuleEvaluatedEvent;
import com.fraudshield.fraud.service.FraudProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleConsumerTest {

    @Mock
    private FraudContextCache cache;

    @Mock
    private FraudProcessor processor;

    @InjectMocks
    private RuleConsumer ruleConsumer;

    private RuleEvaluatedEvent event;
    private UUID transactionId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        event = RuleEvaluatedEvent.builder()
                .transactionId(transactionId)
                .customerId(customerId.toString())
                .ruleScore(60)
                .build();
    }

    @Test
    void shouldSaveRuleAndProcessTransaction() {

        ruleConsumer.consume(event);

        verify(cache).saveRule(
                transactionId,
                customerId,
                60,
                Collections.emptyList());

        verify(processor).process(transactionId);
    }

    @Test
    void shouldSaveRuleWithNullCustomerId() {

        RuleEvaluatedEvent event = RuleEvaluatedEvent.builder()
                .transactionId(transactionId)
                .customerId(null)
                .ruleScore(25)
                .build();

        ruleConsumer.consume(event);

        verify(cache).saveRule(
                transactionId,
                null,
                25,
                Collections.emptyList());

        verify(processor).process(transactionId);
    }

    @Test
    void shouldInvokeSaveRuleBeforeProcessing() {

        ruleConsumer.consume(event);

        InOrder inOrder = inOrder(cache, processor);

        inOrder.verify(cache).saveRule(
                transactionId,
                customerId,
                60,
                Collections.emptyList());

        inOrder.verify(processor).process(transactionId);
    }
}
package com.securepay.fraud.consumer;

import com.securepay.fraud.cache.FraudContextCache;
import com.securepay.fraud.event.RiskAssessedEvent;
import com.securepay.fraud.service.FraudProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskConsumerTest {

    @Mock
    private FraudContextCache cache;

    @Mock
    private FraudProcessor processor;

    @InjectMocks
    private RiskConsumer riskConsumer;

    private RiskAssessedEvent event;

    @BeforeEach
    void setUp() {
        event = RiskAssessedEvent.builder()
                .transactionId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .riskScore(75)
                .build();
    }

    @Test
    void shouldSaveRiskAndProcessTransaction() {

        riskConsumer.consume(event);

        verify(cache).saveRisk(
                event.getTransactionId(),
                event.getCustomerId(),
                event.getRiskScore());

        verify(processor).process(event.getTransactionId());
    }

    @Test
    void shouldCallSaveRiskBeforeProcessing() {

        riskConsumer.consume(event);

        var inOrder = inOrder(cache, processor);

        inOrder.verify(cache).saveRisk(
                event.getTransactionId(),
                event.getCustomerId(),
                event.getRiskScore());

        inOrder.verify(processor)
                .process(event.getTransactionId());
    }

    @Test
    void shouldProcessEventWithNullCustomerId() {

        RiskAssessedEvent event = RiskAssessedEvent.builder()
                .transactionId(UUID.randomUUID())
                .customerId(null)
                .riskScore(40)
                .build();

        riskConsumer.consume(event);

        verify(cache).saveRisk(
                event.getTransactionId(),
                null,
                event.getRiskScore());

        verify(processor).process(event.getTransactionId());
    }
}
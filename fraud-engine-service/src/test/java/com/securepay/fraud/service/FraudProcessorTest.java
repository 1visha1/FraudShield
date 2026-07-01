package com.securepay.fraud.service;

import com.securepay.fraud.cache.FraudContextCache;
import com.securepay.fraud.entity.FraudDecision;
import com.securepay.fraud.event.FraudDetectedEvent;
import com.securepay.fraud.publisher.FraudPublisher;
import com.securepay.fraud.repository.FraudDecisionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudProcessorTest {

    @Mock
    private FraudContextCache cache;

    @Mock
    private FraudScoringService scoringService;

    @Mock
    private FraudPublisher publisher;

    @Mock
    private FraudDecisionRepository repository;

    @InjectMocks
    private FraudProcessor fraudProcessor;

    @Test
    void shouldReturnWhenCacheNotReady() {

        UUID txnId = UUID.randomUUID();

        when(cache.ready(txnId)).thenReturn(false);

        fraudProcessor.process(txnId);

        verify(scoringService, never()).calculate(any(), any());
        verify(repository, never()).save(any());
        verify(publisher, never()).publish(any());
    }

    @Test
    void shouldProcessAndPublishFraudEvent() {

        UUID txnId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        when(cache.ready(txnId)).thenReturn(true);
        when(cache.risk(txnId)).thenReturn(80);
        when(cache.rule(txnId)).thenReturn(20);
        when(cache.customerId(txnId)).thenReturn(customerId);

        when(scoringService.calculate(80, 20)).thenReturn(90);
        when(scoringService.decision(90)).thenReturn("BLOCK");

        FraudDecision savedDecision = FraudDecision.builder()
                .id(UUID.randomUUID())
                .transactionId(txnId)
                .customerId(customerId)
                .fraudScore(90)
                .decision("BLOCK")
                .build();

        when(repository.save(any(FraudDecision.class))).thenReturn(savedDecision);

        fraudProcessor.process(txnId);

        ArgumentCaptor<FraudDecision> decisionCaptor =
                ArgumentCaptor.forClass(FraudDecision.class);

        verify(repository).save(decisionCaptor.capture());

        FraudDecision decision = decisionCaptor.getValue();

        assertEquals(txnId, decision.getTransactionId());
        assertEquals(customerId, decision.getCustomerId());
        assertEquals(90, decision.getFraudScore());
        assertEquals("BLOCK", decision.getDecision());

        ArgumentCaptor<FraudDetectedEvent> eventCaptor =
                ArgumentCaptor.forClass(FraudDetectedEvent.class);

        verify(publisher).publish(eventCaptor.capture());

        FraudDetectedEvent event = eventCaptor.getValue();

        assertEquals(txnId, event.getTransactionId());
        assertEquals(customerId.toString(), event.getCustomerId());
        assertEquals(90, event.getFraudScore());
        assertEquals("BLOCK", event.getDecision());
    }

    @Test
    void shouldHandleNullCustomerId() {

        UUID txnId = UUID.randomUUID();

        when(cache.ready(txnId)).thenReturn(true);
        when(cache.risk(txnId)).thenReturn(50);
        when(cache.rule(txnId)).thenReturn(30);
        when(cache.customerId(txnId)).thenReturn(null);

        when(scoringService.calculate(50, 30)).thenReturn(60);
        when(scoringService.decision(60)).thenReturn("REVIEW");

        when(repository.save(any(FraudDecision.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        fraudProcessor.process(txnId);

        ArgumentCaptor<FraudDetectedEvent> captor =
                ArgumentCaptor.forClass(FraudDetectedEvent.class);

        verify(publisher).publish(captor.capture());

        assertEquals(null, captor.getValue().getCustomerId());
    }

    @Test
    void shouldCatchExceptionAndNotThrow() {

        UUID txnId = UUID.randomUUID();

        when(cache.ready(txnId)).thenThrow(new RuntimeException("Redis error"));

        assertDoesNotThrow(() -> fraudProcessor.process(txnId));

        verify(repository, never()).save(any());
        verify(publisher, never()).publish(any());
    }
}
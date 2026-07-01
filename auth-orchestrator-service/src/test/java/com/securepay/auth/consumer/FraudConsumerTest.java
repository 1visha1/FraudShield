package com.securepay.auth.consumer;

import com.securepay.auth.event.FraudDetectedEvent;
import com.securepay.auth.service.AuthOrchestratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudConsumerTest {

    @Mock
    private AuthOrchestratorService authOrchestratorService;

    @InjectMocks
    private FraudConsumer fraudConsumer;

    private FraudDetectedEvent event;

    @BeforeEach
    void setUp() {
        event = new FraudDetectedEvent();
        event.setTransactionId(UUID.randomUUID());
        event.setCustomerId(UUID.randomUUID());
        event.setFraudScore(75);
        event.setDecision("STEP_UP_AUTH");
        event.setMatchedRules(Arrays.asList("RULE_1", "RULE_2"));
    }

    @Test
    void consume_ShouldInvokeProcess() {

        fraudConsumer.consume(event);

        verify(authOrchestratorService, times(1))
                .process(event);

        verifyNoMoreInteractions(authOrchestratorService);
    }

    @Test
    void consume_ShouldHandleApproveDecision() {

        event.setDecision("APPROVE");

        fraudConsumer.consume(event);

        verify(authOrchestratorService)
                .process(event);
    }

    @Test
    void consume_ShouldHandleBlockDecision() {

        event.setDecision("BLOCK");

        fraudConsumer.consume(event);

        verify(authOrchestratorService)
                .process(event);
    }

    @Test
    void consume_ShouldHandleNullDecision() {

        event.setDecision(null);

        fraudConsumer.consume(event);

        verify(authOrchestratorService)
                .process(event);
    }

    @Test
    void consume_ShouldHandleNullMatchedRules() {

        event.setMatchedRules(null);

        fraudConsumer.consume(event);

        verify(authOrchestratorService)
                .process(event);
    }
}
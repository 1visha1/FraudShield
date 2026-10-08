package com.fraudshield.fraud.publisher;

import com.fraudshield.fraud.event.FraudDetectedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FraudPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private FraudPublisher fraudPublisher;

    private FraudDetectedEvent event;

    @BeforeEach
    void setUp() {
        event = FraudDetectedEvent.builder()
                .transactionId(UUID.randomUUID())
                .customerId(UUID.randomUUID().toString())
                .fraudScore(95)
                .decision("BLOCK")
                .build();
    }

    @Test
    void shouldPublishFraudDetectedEvent() {

        fraudPublisher.publish(event);

        verify(rabbitTemplate).convertAndSend(
                "fraudshield.exchange",
                "fraud.detected",
                event
        );
    }
}
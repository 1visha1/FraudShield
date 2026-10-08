package com.fraudshield.auth.publisher;

import com.fraudshield.auth.event.AuthChallengeCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthChallengePublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthChallengePublisher publisher;

    private AuthChallengeCreatedEvent event;

    @BeforeEach
    void setUp() {

        event = AuthChallengeCreatedEvent.builder()
                .transactionId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .authType("SMS_OTP")
                .status("PENDING")
                .build();
    }

    @Test
    void publish_ShouldSendMessageToRabbitMQ() {

        publisher.publish(event);

        ArgumentCaptor<AuthChallengeCreatedEvent> captor =
                ArgumentCaptor.forClass(AuthChallengeCreatedEvent.class);

        verify(rabbitTemplate, times(1))
                .convertAndSend(
                        eq("fraudshield.exchange"),
                        eq("auth.challenge.created"),
                        captor.capture());

        AuthChallengeCreatedEvent published = captor.getValue();

        assertEquals(event.getTransactionId(), published.getTransactionId());
        assertEquals(event.getCustomerId(), published.getCustomerId());
        assertEquals(event.getAuthType(), published.getAuthType());
        assertEquals(event.getStatus(), published.getStatus());

        verifyNoMoreInteractions(rabbitTemplate);
    }

    @Test
    void publish_ShouldSendSameEventInstance() {

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend(
                "fraudshield.exchange",
                "auth.challenge.created",
                event
        );
    }
}
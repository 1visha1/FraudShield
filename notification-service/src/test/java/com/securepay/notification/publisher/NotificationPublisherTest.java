package com.securepay.notification.publisher;

import com.securepay.notification.event.AuthChallengeSentEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationPublisher notificationPublisher;

    @Test
    void publish_ShouldPublishAuthChallengeSentEvent() {

        AuthChallengeSentEvent event = AuthChallengeSentEvent.builder()
                .transactionId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .authType("OTP")
                .deliveryStatus("SENT")
                .build();

        notificationPublisher.publish(event);

        verify(rabbitTemplate, times(1))
                .convertAndSend(
                        "securepay.exchange",
                        "auth.challenge.sent",
                        event
                );
    }
}
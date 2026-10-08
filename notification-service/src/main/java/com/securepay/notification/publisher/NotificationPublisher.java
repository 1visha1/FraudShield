package com.fraudshield.notification.publisher;
import com.fraudshield.notification.event.AuthChallengeSentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(
            AuthChallengeSentEvent event){

        rabbitTemplate.convertAndSend(
                "fraudshield.exchange",
                "auth.challenge.sent",
                event);
    }
}
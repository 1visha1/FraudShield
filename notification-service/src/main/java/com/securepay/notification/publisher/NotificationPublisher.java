package com.securepay.notification.publisher;
import com.securepay.notification.event.AuthChallengeSentEvent;
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
                "securepay.exchange",
                "auth.challenge.sent",
                event);
    }
}
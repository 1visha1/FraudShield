package com.securepay.auth.publisher;
import com.securepay.auth.event.AuthChallengeCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthChallengePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(
            AuthChallengeCreatedEvent event) {
        log.warn("event: {}",event);
        rabbitTemplate.convertAndSend(
                "securepay.exchange",
                "auth.challenge.created",
                event);
    }
}
package com.fraudshield.risk.publisher;

import com.fraudshield.risk.event.RiskAssessedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RiskEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(
            RiskAssessedEvent event) {

        rabbitTemplate.convertAndSend(
                "fraudshield.exchange",
                "risk.assessed",
                event);
    }
}

package com.securepay.risk.publisher;

import com.securepay.risk.event.RiskAssessedEvent;
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
                "securepay.exchange",
                "risk.assessed",
                event);
    }
}

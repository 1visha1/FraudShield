package com.securepay.risk.publisher;
import com.securepay.risk.event.RuleEvaluatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class RuleEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(
            RuleEvaluatedEvent event) {

        rabbitTemplate.convertAndSend(
                "securepay.exchange",
                "rule.evaluated",
                event);
    }
}
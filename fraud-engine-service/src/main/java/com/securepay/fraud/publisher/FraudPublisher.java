package com.fraudshield.fraud.publisher;
import com.fraudshield.fraud.event.FraudDetectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
@Slf4j
public class FraudPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(
            FraudDetectedEvent event) {
        log.warn("event FraudDetectedEvent {}",event);
        rabbitTemplate.convertAndSend(
                "fraudshield.exchange",
                "fraud.detected",
                event);
    }
}
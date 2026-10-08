package com.fraudshield.auth.consumer;

import com.fraudshield.auth.config.RabbitMQConfig;
import com.fraudshield.auth.event.FraudDetectedEvent;
import com.fraudshield.auth.service.AuthOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudConsumer {

    private final AuthOrchestratorService service;

    @RabbitListener(queues = RabbitMQConfig.FRAUD_QUEUE)
    public void consume(FraudDetectedEvent event) {
        log.info("Received FraudDetectedEvent in Orchestrator: {}", event);
        service.process(event);
    }
}
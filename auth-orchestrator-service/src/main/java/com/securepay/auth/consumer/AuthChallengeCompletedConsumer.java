package com.fraudshield.auth.consumer;

import com.fraudshield.auth.config.RabbitMQConfig;
import com.fraudshield.auth.event.AuthChallengeCompletedEvent;
import com.fraudshield.auth.service.AuthOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthChallengeCompletedConsumer {

    private final AuthOrchestratorService service;

    @RabbitListener(queues = RabbitMQConfig.CHALLENGE_COMPLETED_NOTIFICATION_QUEUE)
    public void consume(AuthChallengeCompletedEvent event) {
        log.info("Received AuthChallengeCompletedEvent in Orchestrator for transaction: {}, status={}", 
                event.getTransactionId(), event.getStatus());
        service.handleChallengeCompletion(event.getTransactionId(), event.getStatus());
    }
}

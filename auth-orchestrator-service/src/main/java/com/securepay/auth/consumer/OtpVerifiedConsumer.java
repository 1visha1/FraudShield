package com.fraudshield.auth.consumer;

import com.fraudshield.auth.config.RabbitMQConfig;
import com.fraudshield.auth.event.OtpVerifiedEvent;
import com.fraudshield.auth.service.AuthOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpVerifiedConsumer {

    private final AuthOrchestratorService service;

    @RabbitListener(queues = RabbitMQConfig.OTP_VERIFIED_QUEUE)
    public void consume(OtpVerifiedEvent event) {
        log.info("Received OtpVerifiedEvent for transaction: {}", event.getTransactionId());
        service.verifyChallenge(event.getTransactionId());
    }
}
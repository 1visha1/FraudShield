package com.securepay.auth.consumer;

import com.securepay.auth.config.RabbitMQConfig;
import com.securepay.auth.event.OtpVerifiedEvent;
import com.securepay.auth.service.AuthOrchestratorService;
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
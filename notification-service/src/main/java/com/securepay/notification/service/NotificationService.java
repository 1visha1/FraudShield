package com.fraudshield.notification.service;

import com.fraudshield.notification.config.RabbitMQConfig;
import com.fraudshield.notification.event.AuthChallengeCreatedEvent;
import com.fraudshield.notification.event.OTPGeneratedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final OtpGenerator otpGenerator;
    private final OtpCacheService otpCacheService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.AUTH_CHALLENGE_QUEUE)
    public void sendNotification(AuthChallengeCreatedEvent event) {
        String otp = otpGenerator.generate();
        
        // Store OTP in Redis
        otpCacheService.save(event.getTransactionId(), otp);
        
        // Output OTP to logs so it can be copied for verification
        log.info("\n======================================================\n" +
                 "🔔 [SIMULATED SMS/EMAIL] -> Sending OTP [{}] \n" +
                 "   Transaction ID: {} \n" +
                 "======================================================", 
                 otp, event.getTransactionId());

        // Publish event to inform orchestrator / other services
        OTPGeneratedEvent sentEvent = OTPGeneratedEvent.builder()
                .transactionId(event.getTransactionId())
                .otp(otp)
                .build();
                
        rabbitTemplate.convertAndSend("fraudshield.exchange", "otp.generated", sentEvent);
    }
}
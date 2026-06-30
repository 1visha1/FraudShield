package com.securepay.notification.controller;

import com.securepay.notification.dto.OtpVerificationRequest;
import com.securepay.notification.event.AuthChallengeCompletedEvent;
import com.securepay.notification.service.OtpCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications/otp")
@RequiredArgsConstructor
@Slf4j
public class OtpController {

    private final OtpCacheService cache;
    private final RabbitTemplate rabbitTemplate;

    @PostMapping("/verify")
    public String verify(@RequestBody OtpVerificationRequest request) {
        log.info("Received OTP verification request for transaction: {}", request.getTransactionId());

        String stored = cache.get(request.getTransactionId());

        if (stored == null) {
            log.warn("OTP expired or not found for transaction: {}", request.getTransactionId());
            publishCompletion(request.getTransactionId(), "FAILED");
            return "OTP_EXPIRED";
        }

        if (!stored.equals(request.getOtp())) {
            log.warn("Invalid OTP entered for transaction: {}", request.getTransactionId());
            publishCompletion(request.getTransactionId(), "FAILED");
            return "OTP_INVALID";
        }

        cache.delete(request.getTransactionId());
        log.info("OTP verified successfully for transaction: {}", request.getTransactionId());

        publishCompletion(request.getTransactionId(), "COMPLETED");
        return "OTP_VERIFIED";
    }

    private void publishCompletion(UUID transactionId, String status) {
        AuthChallengeCompletedEvent event = AuthChallengeCompletedEvent.builder()
                .transactionId(transactionId)
                .status(status)
                .build();
        rabbitTemplate.convertAndSend("securepay.exchange", "auth.challenge.completed.notification", event);
        log.info("Published AuthChallengeCompletedEvent to orchestrator: transaction={}, status={}", transactionId, status);
    }
}
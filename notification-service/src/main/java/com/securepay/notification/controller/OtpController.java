package com.securepay.notification.controller;

import com.securepay.notification.dto.OtpVerificationRequest;
import com.securepay.notification.event.OtpVerifiedEvent;
import com.securepay.notification.service.OtpCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/otp")
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
            return "OTP_EXPIRED";
        }

        if (!stored.equals(request.getOtp())) {
            log.warn("Invalid OTP entered for transaction: {}", request.getTransactionId());
            return "OTP_INVALID";
        }

        cache.delete(request.getTransactionId());
        log.info("OTP verified successfully for transaction: {}", request.getTransactionId());

        // Notify Auth Orchestrator to update session and continue flow
        rabbitTemplate.convertAndSend("securepay.exchange", "otp.verified", 
                OtpVerifiedEvent.builder().transactionId(request.getTransactionId()).build());

        return "OTP_VERIFIED";
    }
}
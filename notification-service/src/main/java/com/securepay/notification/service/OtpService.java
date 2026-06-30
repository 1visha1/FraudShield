package com.securepay.notification.service;

import com.securepay.notification.dto.OtpVerificationRequest;
import com.securepay.notification.dto.OtpVerificationResponse;
import com.securepay.notification.event.OtpVerifiedEvent;
import com.securepay.notification.exception.OtpExpiredException;
import com.securepay.notification.exception.OtpInvalidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpCacheService cache;
    private final RabbitTemplate rabbitTemplate;

    public OtpVerificationResponse verify(OtpVerificationRequest request) {
        log.info("Received OTP verification request for transaction: {}", request.getTransactionId());

        String stored = cache.get(request.getTransactionId());

        if (stored == null) {
            log.warn("OTP expired or not found for transaction: {}", request.getTransactionId());
            throw new OtpExpiredException("OTP expired or not found");
        }

        if (!stored.equals(request.getOtp())) {
            log.warn("Invalid OTP entered for transaction: {}", request.getTransactionId());
            throw new OtpInvalidException("Invalid OTP");
        }

        cache.delete(request.getTransactionId());
        log.info("OTP verified successfully for transaction: {}", request.getTransactionId());

        // Notify Auth Orchestrator to update session and continue flow
        rabbitTemplate.convertAndSend("securepay.exchange", "otp.verified",
                OtpVerifiedEvent.builder().transactionId(request.getTransactionId()).build());

        return OtpVerificationResponse.builder().status("OTP_VERIFIED").build();
    }
}

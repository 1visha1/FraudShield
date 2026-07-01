package com.securepay.notification.controller;

import com.securepay.notification.dto.OtpVerificationRequest;
import com.securepay.notification.dto.OtpVerificationResponse;
import com.securepay.notification.event.AuthChallengeCompletedEvent;
import com.securepay.notification.service.OtpCacheService;
import com.securepay.notification.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications/otp")
@RequiredArgsConstructor
@Slf4j
public class OtpController {

    private final OtpCacheService cache;
    private final RabbitTemplate rabbitTemplate;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<OtpVerificationResponse>> verify(
            @RequestBody OtpVerificationRequest request) {

        log.info("Received OTP verification request for transaction: {}",
                request.getTransactionId());

        String stored = cache.get(request.getTransactionId());

        if (stored == null) {

            publishCompletion(request.getTransactionId(), "FAILED");

            return ResponseEntity.status(HttpStatus.GONE)
                    .body(ApiResponse.<OtpVerificationResponse>builder()
                            .success(false)
                            .message("OTP has expired.")
                            .data(OtpVerificationResponse.builder()
                                    .transactionId(request.getTransactionId())
                                    .status("OTP_EXPIRED")
                                    .build())
                            .build());
        }

        if (!stored.equals(request.getOtp())) {

            publishCompletion(request.getTransactionId(), "FAILED");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<OtpVerificationResponse>builder()
                            .success(false)
                            .message("Invalid OTP.")
                            .data(OtpVerificationResponse.builder()
                                    .transactionId(request.getTransactionId())
                                    .status("OTP_INVALID")
                                    .build())
                            .build());
        }

        cache.delete(request.getTransactionId());

        publishCompletion(request.getTransactionId(), "COMPLETED");

        return ResponseEntity.ok(
                ApiResponse.<OtpVerificationResponse>builder()
                        .success(true)
                        .message("OTP verified successfully.")
                        .data(OtpVerificationResponse.builder()
                                .transactionId(request.getTransactionId())
                                .status("OTP_VERIFIED")
                                .build())
                        .build());
    }

    private void publishCompletion(UUID transactionId, String status) {

        AuthChallengeCompletedEvent event = AuthChallengeCompletedEvent.builder()
                .transactionId(transactionId)
                .status(status)
                .build();

        rabbitTemplate.convertAndSend(
                "securepay.exchange",
                "auth.challenge.completed.notification",
                event);

        log.info(
                "Published AuthChallengeCompletedEvent: transaction={}, status={}",
                transactionId,
                status);
    }
}
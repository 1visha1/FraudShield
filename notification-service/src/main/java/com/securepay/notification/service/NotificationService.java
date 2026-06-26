package com.securepay.notification.service;

import com.securepay.notification.event.AuthChallengeCreatedEvent;
import com.securepay.notification.event.AuthChallengeSentEvent;
import com.securepay.notification.publisher.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final OtpGenerator otpGenerator;
    private final OtpCacheService cacheService;
    private final SmsService smsService;
    private final EmailService emailService;
    private final NotificationPublisher publisher;

    public void process(AuthChallengeCreatedEvent event) {
        log.info("Processing notification request for transaction: {}, customer: {}, authType: {}", 
                event.getTransactionId(), event.getCustomerId(), event.getAuthType());

        try {
            String otp = otpGenerator.generate();
            log.info("OTP GENERATED: [{}] for Transaction: {}", otp, event.getTransactionId());

            cacheService.save(event.getTransactionId(), otp);

            switch (event.getAuthType()) {
                case "SMS_OTP" -> {
                    log.info("Sending SMS OTP...");
                    smsService.send(event.getTransactionId(), otp);
                }
                case "SMS_EMAIL_OTP" -> {
                    log.info("Sending SMS and Email OTP...");
                    smsService.send(event.getTransactionId(), otp);
                    emailService.send(event.getTransactionId(), otp);
                }
                case "PUSH_APPROVAL" -> {
                    log.info("Sending PUSH NOTIFICATION OTP...");
                    // Using SMS service mock as a placeholder to show OTP in console
                    smsService.send(event.getTransactionId(), otp);
                }
                default -> {
                    log.warn("Auth type {} not fully implemented, falling back to SMS log", event.getAuthType());
                    smsService.send(event.getTransactionId(), otp);
                }
            }

            AuthChallengeSentEvent sentEvent = AuthChallengeSentEvent.builder()
                    .transactionId(event.getTransactionId())
                    .customerId(event.getCustomerId())
                    .authType(event.getAuthType())
                    .deliveryStatus("SENT")
                    .build();

            publisher.publish(sentEvent);
            log.info("Successfully processed notification for transaction: {}", event.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to process notification request for transaction: {}. Error: {}", 
                    event.getTransactionId(), e.getMessage(), e);
        }
    }
}
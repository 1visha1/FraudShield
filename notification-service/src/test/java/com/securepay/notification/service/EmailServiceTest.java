package com.fraudshield.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EmailServiceTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService();
    }

    @Test
    void send_ShouldExecuteWithoutException() {

        UUID transactionId = UUID.randomUUID();
        String otp = "123456";

        assertDoesNotThrow(() ->
                emailService.send(transactionId, otp)
        );
    }

    @Test
    void send_ShouldHandleNullOtpWithoutException() {

        UUID transactionId = UUID.randomUUID();

        assertDoesNotThrow(() ->
                emailService.send(transactionId, null)
        );
    }

    @Test
    void send_ShouldHandleNullTransactionIdWithoutException() {

        String otp = "654321";

        assertDoesNotThrow(() ->
                emailService.send(null, otp)
        );
    }
}
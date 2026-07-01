package com.securepay.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SmsServiceTest {

    private SmsService smsService;

    @BeforeEach
    void setUp() {
        smsService = new SmsService();
    }

    @Test
    void send_ShouldExecuteWithoutException() {

        UUID transactionId = UUID.randomUUID();
        String otp = "123456";

        assertDoesNotThrow(() ->
                smsService.send(transactionId, otp)
        );
    }

    @Test
    void send_ShouldHandleNullOtpWithoutException() {

        UUID transactionId = UUID.randomUUID();

        assertDoesNotThrow(() ->
                smsService.send(transactionId, null)
        );
    }

    @Test
    void send_ShouldHandleNullTransactionIdWithoutException() {

        String otp = "654321";

        assertDoesNotThrow(() ->
                smsService.send(null, otp)
        );
    }
}
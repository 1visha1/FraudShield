package com.securepay.auth.service;

import com.securepay.auth.dto.AuthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthDecisionServiceTest {

    private AuthDecisionService authDecisionService;

    @BeforeEach
    void setUp() {
        authDecisionService = new AuthDecisionService();
    }

    @Test
    void determine_ShouldReturnNone_WhenDecisionIsApprove() {
        // Act
        AuthType result = authDecisionService.determine("APPROVE", 10);

        // Assert
        assertEquals(AuthType.NONE, result);
    }

    @Test
    void determine_ShouldReturnSmsOtp_WhenDecisionIsStepUpAuth() {
        // Act
        AuthType result = authDecisionService.determine("STEP_UP_AUTH", 50);

        // Assert
        assertEquals(AuthType.SMS_OTP, result);
    }

    @Test
    void determine_ShouldReturnSmsEmailOtp_WhenDecisionIsHighRisk() {
        // Act
        AuthType result = authDecisionService.determine("HIGH_RISK", 90);

        // Assert
        assertEquals(AuthType.SMS_EMAIL_OTP, result);
    }

    @Test
    void determine_ShouldReturnNone_WhenDecisionIsBlock() {
        // Act
        AuthType result = authDecisionService.determine("BLOCK", 100);

        // Assert
        assertEquals(AuthType.NONE, result);
    }

    @Test
    void determine_ShouldReturnNone_WhenDecisionIsUnknown() {
        // Act
        AuthType result = authDecisionService.determine("UNKNOWN", 20);

        // Assert
        assertEquals(AuthType.NONE, result);
    }

    @Test
    void determine_ShouldReturnNone_WhenDecisionIsNull() {
        // Act
        AuthType result = authDecisionService.determine(null, 30);

        // Assert
        assertEquals(AuthType.NONE, result);
    }
}
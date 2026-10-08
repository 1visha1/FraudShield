package com.fraudshield.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fraudshield.auth.dto.AuthType;

@Service
@Slf4j
public class AuthDecisionService {

    public AuthType determine(String decision, Integer fraudScore) {
        log.info("Determining auth type for decision: {} with score: {}", decision, fraudScore);

        if ("APPROVE".equals(decision)) {
            return AuthType.NONE;
        }

        if ("STEP_UP_AUTH".equals(decision)) {
            return AuthType.SMS_OTP;
        }

        if ("HIGH_RISK".equals(decision)) {
            return AuthType.SMS_EMAIL_OTP;
        }

        // BLOCK should not result in an AuthType because it's a terminal state
        return AuthType.NONE;
    }
}
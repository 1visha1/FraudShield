package com.fraudshield.notification.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OtpVerificationRequest {

    private UUID transactionId;

    private String otp;
}

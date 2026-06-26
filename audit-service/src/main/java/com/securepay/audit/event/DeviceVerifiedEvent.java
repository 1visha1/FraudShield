package com.securepay.audit.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceVerifiedEvent {
    private UUID transactionId; // Added this
    private UUID deviceId;
    private UUID customerId;
    private String fingerprint;
    private Boolean trusted;
    private Integer riskScore;
}
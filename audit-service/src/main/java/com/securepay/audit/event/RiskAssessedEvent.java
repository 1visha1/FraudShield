package com.securepay.audit.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessedEvent {
    private UUID transactionId;
    private Integer riskScore;
    private String riskLevel;
}
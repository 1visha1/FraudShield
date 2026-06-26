package com.securepay.fraud.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessedEvent {
    private UUID transactionId;
    private UUID customerId;
    private Integer riskScore;
    private String riskLevel;
    private Integer deviceRiskScore;
}
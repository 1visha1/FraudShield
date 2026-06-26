package com.securepay.risk.event;

import lombok.*;

import java.math.BigDecimal;
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

    // Required by Rule Engine
    private BigDecimal amount;

    // Required by Rule Engine
    private Boolean deviceTrusted;

    // Optional
    private Integer deviceRiskScore;
}
package com.securepay.rule.event;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class RiskAssessedEvent {

    private UUID transactionId;

    private UUID customerId;

    private Integer riskScore;

    private String riskLevel;

    private BigDecimal amount;

    private Boolean deviceTrusted;
}
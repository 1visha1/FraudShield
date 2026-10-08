package com.fraudshield.risk.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEvaluatedEvent {
    private UUID transactionId;
    private String customerId;
    private BigDecimal amount;
    private int riskScore;
    private int ruleScore;
}
package com.securepay.rule.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Builder
public class RuleContext {

    private BigDecimal amount;

    private Integer riskScore;

    private Boolean deviceTrusted;
}
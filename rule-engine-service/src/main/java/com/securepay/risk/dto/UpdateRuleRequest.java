package com.fraudshield.risk.dto;

import lombok.Data;

@Data
public class UpdateRuleRequest {

    private String ruleName;

    private String ruleExpression;

    private Integer riskScore;

    private Boolean enabled;
}
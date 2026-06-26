package com.securepay.rule.dto;

import lombok.Data;

@Data
public class CreateRuleRequest {

    private String ruleName;

    private Integer ruleVersion;

    private String ruleExpression;

    private Integer riskScore;

    private Boolean enabled;
}
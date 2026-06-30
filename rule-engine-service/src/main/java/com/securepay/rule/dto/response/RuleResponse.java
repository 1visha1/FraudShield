package com.securepay.rule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleResponse {

    private UUID ruleId;
    private String ruleName;
    private Integer ruleVersion;
    private String ruleExpression;
    private Integer riskScore;
    private Boolean enabled;
    private LocalDateTime createdAt;
}

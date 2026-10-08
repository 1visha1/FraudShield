package com.fraudshield.risk.mapper;

import com.fraudshield.risk.dto.CreateRuleRequest;
import com.fraudshield.risk.dto.response.RuleResponse;
import com.fraudshield.risk.entity.FraudRule;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RuleMapper {

    public FraudRule toEntity(CreateRuleRequest request) {
        return FraudRule.builder()
                .ruleName(request.getRuleName())
                .ruleExpression(request.getRuleExpression())
                .riskScore(request.getRiskScore())
                .enabled(true)
                .ruleVersion(1)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public RuleResponse toResponse(FraudRule rule) {
        return RuleResponse.builder()
                .ruleId(rule.getRuleId())
                .ruleName(rule.getRuleName())
                .ruleVersion(rule.getRuleVersion())
                .ruleExpression(rule.getRuleExpression())
                .riskScore(rule.getRiskScore())
                .enabled(rule.getEnabled())
                .createdAt(rule.getCreatedAt())
                .build();
    }
}

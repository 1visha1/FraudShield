package com.securepay.risk.service;


import com.securepay.risk.dto.CreateRuleRequest;
import com.securepay.risk.dto.UpdateRuleRequest;
import com.securepay.risk.entity.FraudRule;
import com.securepay.risk.repository.FraudRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RuleManagementService {

    private final FraudRuleRepository repository;

    public FraudRule create(CreateRuleRequest request) {

        FraudRule rule =
                FraudRule.builder()
                        .ruleName(request.getRuleName())
                        .ruleVersion(request.getRuleVersion())
                        .ruleExpression(request.getRuleExpression())
                        .riskScore(request.getRiskScore())
                        .enabled(request.getEnabled())
                        .createdAt(LocalDateTime.now())
                        .build();

        return repository.save(rule);
    }

    public List<FraudRule> getAll() {
        return repository.findAll();
    }

    public FraudRule getById(UUID id) {
        return repository.findById(id)
                .orElseThrow();
    }

    public FraudRule update(
            UUID id,
            UpdateRuleRequest request) {

        FraudRule rule =
                repository.findById(id)
                        .orElseThrow();

        rule.setRuleName(request.getRuleName());
        rule.setRuleExpression(
                request.getRuleExpression());
        rule.setRiskScore(
                request.getRiskScore());
        rule.setEnabled(
                request.getEnabled());

        return repository.save(rule);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public FraudRule enable(UUID id) {

        FraudRule rule =
                repository.findById(id)
                        .orElseThrow();

        rule.setEnabled(true);

        return repository.save(rule);
    }

    public FraudRule disable(UUID id) {

        FraudRule rule =
                repository.findById(id)
                        .orElseThrow();

        rule.setEnabled(false);

        return repository.save(rule);
    }
}
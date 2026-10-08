package com.fraudshield.risk.config;

import com.fraudshield.risk.entity.FraudRule;
import com.fraudshield.risk.repository.FraudRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RuleDataInitializer implements CommandLineRunner {

    private final FraudRuleRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            
            // Rule 1: High Transaction Amount
            repository.save(FraudRule.builder()
                    .ruleName("HIGH_AMOUNT")
                    .ruleExpression("#amount > 100000")
                    .riskScore(40)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build());

            // Rule 2: Untrusted Device
            repository.save(FraudRule.builder()
                    .ruleName("UNTRUSTED_DEVICE")
                    .ruleExpression("#deviceTrusted == false")
                    .riskScore(60)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build());

            // Rule 3: Very High Amount (Critical)
            repository.save(FraudRule.builder()
                    .ruleName("CRITICAL_AMOUNT")
                    .ruleExpression("#amount > 500000")
                    .riskScore(100)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }
}
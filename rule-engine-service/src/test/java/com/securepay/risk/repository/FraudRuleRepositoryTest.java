package com.fraudshield.risk.repository;

import com.fraudshield.risk.entity.FraudRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class FraudRuleRepositoryTest {

    @Autowired
    private FraudRuleRepository repository;

    @Test
    @DisplayName("Should return only enabled fraud rules")
    void findByEnabledTrue_ShouldReturnEnabledRulesOnly() {

        FraudRule enabledRule1 = FraudRule.builder()
                .ruleName("High Amount")
                .ruleVersion(1)
                .ruleExpression("amount > 1000")
                .riskScore(80)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        FraudRule enabledRule2 = FraudRule.builder()
                .ruleName("High Risk")
                .ruleVersion(1)
                .ruleExpression("riskScore > 70")
                .riskScore(90)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        FraudRule disabledRule = FraudRule.builder()
                .ruleName("Disabled Rule")
                .ruleVersion(1)
                .ruleExpression("amount > 500")
                .riskScore(30)
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(enabledRule1);
        repository.save(enabledRule2);
        repository.save(disabledRule);

        List<FraudRule> result = repository.findByEnabledTrue();

        assertEquals(2, result.size());

        assertTrue(result.stream().allMatch(FraudRule::getEnabled));

        assertTrue(result.stream()
                .anyMatch(rule -> rule.getRuleName().equals("High Amount")));

        assertTrue(result.stream()
                .anyMatch(rule -> rule.getRuleName().equals("High Risk")));
    }

    @Test
    @DisplayName("Should return empty list when no enabled rules exist")
    void findByEnabledTrue_ShouldReturnEmptyList() {

        FraudRule disabledRule = FraudRule.builder()
                .ruleName("Disabled Rule")
                .ruleVersion(1)
                .ruleExpression("amount > 100")
                .riskScore(50)
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(disabledRule);

        List<FraudRule> result = repository.findByEnabledTrue();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should save fraud rule successfully")
    void save_ShouldPersistFraudRule() {

        FraudRule rule = FraudRule.builder()
                .ruleName("Velocity Rule")
                .ruleVersion(2)
                .ruleExpression("txnCount > 5")
                .riskScore(60)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        FraudRule saved = repository.save(rule);

        assertNotNull(saved.getRuleId());
        assertEquals("Velocity Rule", saved.getRuleName());
        assertEquals(60, saved.getRiskScore());
        assertTrue(saved.getEnabled());
    }

    @Test
    @DisplayName("Should find fraud rule by id")
    void findById_ShouldReturnRule() {

        FraudRule rule = FraudRule.builder()
                .ruleName("Geo Rule")
                .ruleVersion(1)
                .ruleExpression("country != homeCountry")
                .riskScore(75)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        FraudRule saved = repository.save(rule);

        FraudRule result = repository.findById(saved.getRuleId()).orElse(null);

        assertNotNull(result);
        assertEquals(saved.getRuleId(), result.getRuleId());
        assertEquals("Geo Rule", result.getRuleName());
    }
}
package com.securepay.risk.repository;

import com.securepay.risk.model.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RuleRepositoryTest {

    @Autowired
    private RuleRepository repository;

    @Test
    @DisplayName("Should return only enabled rules")
    void findAllByEnabledTrue_ShouldReturnEnabledRulesOnly() {

        Rule enabledRule1 = Rule.builder()
                .name("Rule One")
                .description("First Rule")
                .expression("amount > 100")
                .priority(1)
                .weight(10)
                .enabled(true)
                .version(1)
                .build();

        Rule enabledRule2 = Rule.builder()
                .name("Rule Two")
                .description("Second Rule")
                .expression("riskScore > 50")
                .priority(2)
                .weight(20)
                .enabled(true)
                .version(1)
                .build();

        Rule disabledRule = Rule.builder()
                .name("Disabled Rule")
                .description("Disabled")
                .expression("amount > 500")
                .priority(3)
                .weight(30)
                .enabled(false)
                .version(1)
                .build();

        repository.save(enabledRule1);
        repository.save(enabledRule2);
        repository.save(disabledRule);

        List<Rule> result = repository.findAllByEnabledTrue();

        assertEquals(2, result.size());

        assertTrue(result.stream().allMatch(Rule::isEnabled));

        assertTrue(result.stream()
                .anyMatch(rule -> rule.getName().equals("Rule One")));

        assertTrue(result.stream()
                .anyMatch(rule -> rule.getName().equals("Rule Two")));
    }

    @Test
    @DisplayName("Should return empty list when no enabled rules exist")
    void findAllByEnabledTrue_ShouldReturnEmptyList() {

        Rule disabledRule = Rule.builder()
                .name("Disabled")
                .description("Disabled")
                .expression("amount > 100")
                .priority(1)
                .weight(10)
                .enabled(false)
                .version(1)
                .build();

        repository.save(disabledRule);

        List<Rule> result = repository.findAllByEnabledTrue();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
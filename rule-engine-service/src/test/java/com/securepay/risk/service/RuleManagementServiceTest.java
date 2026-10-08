package com.fraudshield.risk.service;

import com.fraudshield.risk.dto.CreateRuleRequest;
import com.fraudshield.risk.dto.UpdateRuleRequest;
import com.fraudshield.risk.entity.FraudRule;
import com.fraudshield.risk.repository.FraudRuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleManagementServiceTest {

    @Mock
    private FraudRuleRepository repository;

    @InjectMocks
    private RuleManagementService service;

    @Test
    void create_ShouldCreateRuleSuccessfully() {

        CreateRuleRequest request = new CreateRuleRequest();
        request.setRuleName("High Amount");
        request.setRuleVersion(1);
        request.setRuleExpression("amount > 1000");
        request.setRiskScore(50);
        request.setEnabled(true);

        FraudRule saved = FraudRule.builder()
                .ruleId(UUID.randomUUID())
                .ruleName(request.getRuleName())
                .ruleVersion(request.getRuleVersion())
                .ruleExpression(request.getRuleExpression())
                .riskScore(request.getRiskScore())
                .enabled(true)
                .build();

        when(repository.save(any(FraudRule.class))).thenReturn(saved);

        FraudRule result = service.create(request);

        assertNotNull(result);
        assertEquals("High Amount", result.getRuleName());
        assertEquals(50, result.getRiskScore());
        assertTrue(result.getEnabled());

        verify(repository).save(any(FraudRule.class));
    }

    @Test
    void getAll_ShouldReturnAllRules() {

        FraudRule rule1 = FraudRule.builder()
                .ruleId(UUID.randomUUID())
                .ruleName("Rule1")
                .build();

        FraudRule rule2 = FraudRule.builder()
                .ruleId(UUID.randomUUID())
                .ruleName("Rule2")
                .build();

        when(repository.findAll()).thenReturn(List.of(rule1, rule2));

        List<FraudRule> result = service.getAll();

        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    @Test
    void getById_ShouldReturnRule() {

        UUID ruleId = UUID.randomUUID();

        FraudRule rule = FraudRule.builder()
                .ruleId(ruleId)
                .ruleName("Rule")
                .build();

        when(repository.findById(ruleId))
                .thenReturn(Optional.of(rule));

        FraudRule result = service.getById(ruleId);

        assertEquals(ruleId, result.getRuleId());

        verify(repository).findById(ruleId);
    }

    @Test
    void getById_ShouldThrowException_WhenRuleNotFound() {

        UUID ruleId = UUID.randomUUID();

        when(repository.findById(ruleId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.getById(ruleId));

        verify(repository).findById(ruleId);
    }

    @Test
    void update_ShouldUpdateRuleSuccessfully() {

        UUID ruleId = UUID.randomUUID();

        FraudRule existing = FraudRule.builder()
                .ruleId(ruleId)
                .ruleName("Old")
                .ruleExpression("old")
                .riskScore(10)
                .enabled(false)
                .build();

        UpdateRuleRequest request = new UpdateRuleRequest();
        request.setRuleName("New Rule");
        request.setRuleExpression("amount > 500");
        request.setRiskScore(80);
        request.setEnabled(true);

        when(repository.findById(ruleId))
                .thenReturn(Optional.of(existing));

        when(repository.save(any(FraudRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FraudRule result = service.update(ruleId, request);

        assertEquals("New Rule", result.getRuleName());
        assertEquals("amount > 500", result.getRuleExpression());
        assertEquals(80, result.getRiskScore());
        assertTrue(result.getEnabled());

        verify(repository).save(existing);
    }

    @Test
    void update_ShouldThrowException_WhenRuleNotFound() {

        UUID ruleId = UUID.randomUUID();

        when(repository.findById(ruleId))
                .thenReturn(Optional.empty());

        UpdateRuleRequest request = new UpdateRuleRequest();

        assertThrows(RuntimeException.class,
                () -> service.update(ruleId, request));

        verify(repository, never()).save(any());
    }

    @Test
    void delete_ShouldDeleteRule() {

        UUID ruleId = UUID.randomUUID();

        service.delete(ruleId);

        verify(repository).deleteById(ruleId);
    }

    @Test
    void enable_ShouldEnableRule() {

        UUID ruleId = UUID.randomUUID();

        FraudRule rule = FraudRule.builder()
                .ruleId(ruleId)
                .enabled(false)
                .build();

        when(repository.findById(ruleId))
                .thenReturn(Optional.of(rule));

        when(repository.save(any(FraudRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FraudRule result = service.enable(ruleId);

        assertTrue(result.getEnabled());

        verify(repository).save(rule);
    }

    @Test
    void disable_ShouldDisableRule() {

        UUID ruleId = UUID.randomUUID();

        FraudRule rule = FraudRule.builder()
                .ruleId(ruleId)
                .enabled(true)
                .build();

        when(repository.findById(ruleId))
                .thenReturn(Optional.of(rule));

        when(repository.save(any(FraudRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FraudRule result = service.disable(ruleId);

        assertFalse(result.getEnabled());

        verify(repository).save(rule);
    }

    @Test
    void enable_ShouldThrowException_WhenRuleNotFound() {

        UUID ruleId = UUID.randomUUID();

        when(repository.findById(ruleId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.enable(ruleId));

        verify(repository, never()).save(any());
    }

    @Test
    void disable_ShouldThrowException_WhenRuleNotFound() {

        UUID ruleId = UUID.randomUUID();

        when(repository.findById(ruleId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.disable(ruleId));

        verify(repository, never()).save(any());
    }
}
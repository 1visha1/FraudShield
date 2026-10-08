package com.fraudshield.risk.mapper;

import com.fraudshield.risk.dto.CreateRuleRequest;
import com.fraudshield.risk.dto.response.RuleResponse;
import com.fraudshield.risk.entity.FraudRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RuleMapperTest {

    private RuleMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RuleMapper();
    }

    @Test
    void toEntity_ShouldMapCreateRuleRequestToFraudRule() {

        CreateRuleRequest request = new CreateRuleRequest();
        request.setRuleName("High Amount Rule");
        request.setRuleExpression("amount > 1000");
        request.setRiskScore(80);

        FraudRule result = mapper.toEntity(request);

        assertNotNull(result);

        assertNull(result.getRuleId());
        assertEquals("High Amount Rule", result.getRuleName());
        assertEquals("amount > 1000", result.getRuleExpression());
        assertEquals(Integer.valueOf(80), result.getRiskScore());

        assertTrue(result.getEnabled());
        assertEquals(Integer.valueOf(1), result.getRuleVersion());

        assertNotNull(result.getCreatedAt());

        assertTrue(
                result.getCreatedAt()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void toResponse_ShouldMapFraudRuleToRuleResponse() {

        UUID ruleId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        FraudRule rule = FraudRule.builder()
                .ruleId(ruleId)
                .ruleName("Velocity Rule")
                .ruleVersion(2)
                .ruleExpression("txnCount > 5")
                .riskScore(60)
                .enabled(false)
                .createdAt(createdAt)
                .build();

        RuleResponse response = mapper.toResponse(rule);

        assertNotNull(response);

        assertEquals(ruleId, response.getRuleId());
        assertEquals("Velocity Rule", response.getRuleName());
        assertEquals(Integer.valueOf(2), response.getRuleVersion());
        assertEquals("txnCount > 5", response.getRuleExpression());
        assertEquals(Integer.valueOf(60), response.getRiskScore());
        assertFalse(response.getEnabled());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
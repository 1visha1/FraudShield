package com.fraudshield.risk.service;

import com.fraudshield.risk.config.RabbitMQConfig;
import com.fraudshield.risk.event.RiskAssessedEvent;
import com.fraudshield.risk.event.RuleEvaluatedEvent;
import com.fraudshield.risk.model.Rule;
import com.fraudshield.risk.repository.RuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleEvaluationServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RuleRepository ruleRepository;

    @InjectMocks
    private RuleEvaluationService ruleEvaluationService;

    @Test
    void evaluateRules_ShouldPublishZeroScore_WhenNoRulesExist() {

        RiskAssessedEvent event = RiskAssessedEvent.builder()
                .transactionId(UUID.randomUUID())
                .customerId("CUST001")
                .amount(BigDecimal.valueOf(500))
                .riskScore(20)
                .build();

        when(ruleRepository.findAllByEnabledTrue())
                .thenReturn(Collections.emptyList());

        ruleEvaluationService.evaluateRules(event);

        ArgumentCaptor<RuleEvaluatedEvent> captor =
                ArgumentCaptor.forClass(RuleEvaluatedEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.RULE_EVALUATED_KEY),
                captor.capture()
        );

        RuleEvaluatedEvent published = captor.getValue();

        assertEquals(0, published.getRuleScore());
        assertEquals(event.getTransactionId(), published.getTransactionId());
        assertEquals(event.getCustomerId(), published.getCustomerId());
    }

    @Test
    void evaluateRules_ShouldCalculateRuleScore_WhenRulesMatch() {

        Rule rule1 = Rule.builder()
                .id(1L)
                .expression("amount > 100")
                .weight(20)
                .enabled(true)
                .build();

        Rule rule2 = Rule.builder()
                .id(2L)
                .expression("riskScore > 50")
                .weight(30)
                .enabled(true)
                .build();

        RiskAssessedEvent event = RiskAssessedEvent.builder()
                .transactionId(UUID.randomUUID())
                .customerId("CUST001")
                .amount(BigDecimal.valueOf(1000))
                .riskScore(80)
                .build();

        when(ruleRepository.findAllByEnabledTrue())
                .thenReturn(List.of(rule1, rule2));

        ruleEvaluationService.evaluateRules(event);

        ArgumentCaptor<RuleEvaluatedEvent> captor =
                ArgumentCaptor.forClass(RuleEvaluatedEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.RULE_EVALUATED_KEY),
                captor.capture()
        );

        RuleEvaluatedEvent published = captor.getValue();

        assertEquals(50, published.getRuleScore());
    }

    @Test
    void evaluateRules_ShouldPublishZero_WhenRulesDoNotMatch() {

        Rule rule = Rule.builder()
                .id(1L)
                .expression("amount > 5000")
                .weight(40)
                .enabled(true)
                .build();

        RiskAssessedEvent event = RiskAssessedEvent.builder()
                .transactionId(UUID.randomUUID())
                .customerId("CUST002")
                .amount(BigDecimal.valueOf(100))
                .riskScore(5)
                .build();

        when(ruleRepository.findAllByEnabledTrue())
                .thenReturn(List.of(rule));

        ruleEvaluationService.evaluateRules(event);

        ArgumentCaptor<RuleEvaluatedEvent> captor =
                ArgumentCaptor.forClass(RuleEvaluatedEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.RULE_EVALUATED_KEY),
                captor.capture()
        );

        RuleEvaluatedEvent published = captor.getValue();

        assertEquals(0, published.getRuleScore());
    }

    @Test
    void evaluateRules_ShouldIgnoreInvalidExpression_AndContinue() {

        Rule invalidRule = Rule.builder()
                .id(1L)
                .expression("this is invalid mvel")
                .weight(100)
                .enabled(true)
                .build();

        Rule validRule = Rule.builder()
                .id(2L)
                .expression("amount > 100")
                .weight(25)
                .enabled(true)
                .build();

        RiskAssessedEvent event = RiskAssessedEvent.builder()
                .transactionId(UUID.randomUUID())
                .customerId("CUST003")
                .amount(BigDecimal.valueOf(500))
                .riskScore(30)
                .build();

        when(ruleRepository.findAllByEnabledTrue())
                .thenReturn(List.of(invalidRule, validRule));

        ruleEvaluationService.evaluateRules(event);

        ArgumentCaptor<RuleEvaluatedEvent> captor =
                ArgumentCaptor.forClass(RuleEvaluatedEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.RULE_EVALUATED_KEY),
                captor.capture()
        );

        RuleEvaluatedEvent published = captor.getValue();

        assertEquals(25, published.getRuleScore());
    }
}
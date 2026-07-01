package com.securepay.risk.service;

import com.securepay.risk.config.RabbitMQConfig;
import com.securepay.risk.event.RiskAssessedEvent;
import com.securepay.risk.event.RuleEvaluatedEvent;
import com.securepay.risk.model.Rule;
import com.securepay.risk.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.MVEL;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEvaluationService {

    private final RabbitTemplate rabbitTemplate;
    private final RuleRepository ruleRepository;

    @RabbitListener(queues = RabbitMQConfig.RISK_ASSESSED_QUEUE)
    public void evaluateRules(RiskAssessedEvent event) {
        log.info("Evaluating rules for transaction: {}", event.getTransactionId());

        List<Rule> rules = ruleRepository.findAllByEnabledTrue();
        int totalScore = 0;
        Map<String, Object> context = new HashMap<>();
        context.put("amount", event.getAmount());
        context.put("riskScore", event.getRiskScore());

        for (Rule rule : rules) {
            try {
                if ((Boolean) MVEL.eval(rule.getExpression(), context)) {
                    totalScore += rule.getWeight();
                }
            } catch (Exception e) {
                log.error("Error evaluating rule: {}", rule.getId(), e);
            }
        }

        RuleEvaluatedEvent ruleEvaluatedEvent = RuleEvaluatedEvent.builder()
                .transactionId(event.getTransactionId())
                .customerId(event.getCustomerId())
                .amount(event.getAmount())
                .riskScore(event.getRiskScore())
                .ruleScore(totalScore)
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RULE_EVALUATED_KEY, ruleEvaluatedEvent);
        log.info("Published RuleEvaluatedEvent for transaction: {}", event.getTransactionId());
    }
}
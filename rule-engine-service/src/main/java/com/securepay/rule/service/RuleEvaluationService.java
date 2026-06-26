package com.securepay.rule.service;
import com.securepay.rule.dto.EvaluationResult;
import com.securepay.rule.dto.RuleContext;
import com.securepay.rule.entity.FraudRule;
import com.securepay.rule.repository.FraudRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class RuleEvaluationService {

    private final FraudRuleRepository repository;

    public EvaluationResult evaluate(
            RuleContext context) {

        int totalScore = 0;

        List<String> matched =
                new ArrayList<>();

        ExpressionParser parser =
                new SpelExpressionParser();

        StandardEvaluationContext evalContext =
                new StandardEvaluationContext();

        evalContext.setVariable(
                "amount",
                context.getAmount());

        evalContext.setVariable(
                "riskScore",
                context.getRiskScore());

        evalContext.setVariable(
                "deviceTrusted",
                context.getDeviceTrusted());

        List<FraudRule> rules =
                repository.findByEnabledTrue();

        for(FraudRule rule : rules) {

            Boolean result =
                    parser.parseExpression(
                                    rule.getRuleExpression())
                            .getValue(
                                    evalContext,
                                    Boolean.class);

            if(Boolean.TRUE.equals(result)) {

                totalScore += rule.getRiskScore();

                matched.add(
                        rule.getRuleName());
            }
        }

        return new EvaluationResult(
                totalScore,
                matched);
    }
}
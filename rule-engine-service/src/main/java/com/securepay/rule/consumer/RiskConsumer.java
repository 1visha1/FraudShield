package com.securepay.rule.consumer;
import com.securepay.rule.dto.EvaluationResult;
import com.securepay.rule.dto.RuleContext;
import com.securepay.rule.event.RiskAssessedEvent;
import com.securepay.rule.event.RuleEvaluatedEvent;
import com.securepay.rule.publisher.RuleEventPublisher;
import com.securepay.rule.service.RuleEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class RiskConsumer {

    private final RuleEvaluationService service;

    private final RuleEventPublisher publisher;

    @RabbitListener(
            queues = "risk.assessed.q")
    public void consume(
            RiskAssessedEvent event) {

        RuleContext context =
                RuleContext.builder()
                        .amount(event.getAmount())
                        .riskScore(
                                event.getRiskScore())
                        .deviceTrusted(
                                event.getDeviceTrusted())
                        .build();

        EvaluationResult result =
                service.evaluate(context);

        publisher.publish(
                RuleEvaluatedEvent.builder()
                        .transactionId(
                                event.getTransactionId())
                        .customerId(
                                event.getCustomerId())
                        .ruleScore(
                                result.getScore())
                        .matchedRules(
                                result.getMatchedRules())
                        .build());
    }
}
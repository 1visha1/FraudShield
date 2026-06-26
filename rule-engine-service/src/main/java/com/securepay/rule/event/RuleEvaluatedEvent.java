package com.securepay.rule.event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEvaluatedEvent {

    private UUID transactionId;

    private UUID customerId;

    private Integer ruleScore;

    private List<String> matchedRules;
}
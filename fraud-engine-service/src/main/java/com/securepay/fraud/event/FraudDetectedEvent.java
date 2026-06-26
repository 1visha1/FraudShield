package com.securepay.fraud.event;

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
public class FraudDetectedEvent {

    private UUID transactionId;

    private UUID customerId;

    private Integer fraudScore;

    private String decision;

    private List<String> matchedRules;
}

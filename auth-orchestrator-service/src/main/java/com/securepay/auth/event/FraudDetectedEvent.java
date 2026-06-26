package com.securepay.auth.event;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class FraudDetectedEvent {

    private UUID transactionId;

    private UUID customerId;

    private Integer fraudScore;

    private String decision;

    private List<String> matchedRules;
}
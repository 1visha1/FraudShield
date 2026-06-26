package com.securepay.audit.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudDetectedEvent {
    private UUID transactionId;
    private UUID customerId;
    private Integer fraudScore;
    private String decision;
}
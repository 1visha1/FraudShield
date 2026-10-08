package com.fraudshield.audit.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionBlockedEvent {
    private UUID transactionId;
    private UUID customerId;
    private String reason;
}
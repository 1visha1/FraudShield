package com.securepay.risk.event;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransactionCreatedEvent {

    private String eventType;

    private UUID transactionId;

    private UUID customerId;

    private BigDecimal amount;
}

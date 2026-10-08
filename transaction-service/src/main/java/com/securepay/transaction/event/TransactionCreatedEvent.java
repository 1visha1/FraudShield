package com.fraudshield.transaction.event;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {

    private String eventType;

    private UUID transactionId;

    private UUID customerId;

    private BigDecimal amount;
}
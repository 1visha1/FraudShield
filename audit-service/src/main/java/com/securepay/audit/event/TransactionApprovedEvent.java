package com.fraudshield.audit.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionApprovedEvent {
    private UUID transactionId;
    private UUID customerId;
    private BigDecimal amount;
    private LocalDateTime approvedAt;
}
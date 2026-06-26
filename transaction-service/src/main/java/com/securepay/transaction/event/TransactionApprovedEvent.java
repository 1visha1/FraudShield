package com.securepay.transaction.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionApprovedEvent {
    private UUID transactionId;
    private UUID customerId;
    private BigDecimal amount;
    private LocalDateTime approvedAt;
}
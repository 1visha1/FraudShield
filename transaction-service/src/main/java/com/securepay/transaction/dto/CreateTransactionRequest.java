package com.fraudshield.transaction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateTransactionRequest {

    @NotNull
    private UUID customerId;

    @NotNull
    private BigDecimal amount;
}
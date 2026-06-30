package com.securepay.transaction.mapper;

import com.securepay.transaction.dto.CreateTransactionRequest;
import com.securepay.transaction.dto.response.TransactionResponse;
import com.securepay.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TransactionMapper {

    public Transaction toEntity(CreateTransactionRequest request) {
        return Transaction.builder()
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .customerId(transaction.getCustomerId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

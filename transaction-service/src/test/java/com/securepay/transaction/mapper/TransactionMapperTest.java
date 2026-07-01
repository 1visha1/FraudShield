package com.securepay.transaction.mapper;

import com.securepay.transaction.dto.CreateTransactionRequest;
import com.securepay.transaction.dto.response.TransactionResponse;
import com.securepay.transaction.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {

    private TransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransactionMapper();
    }

    @Test
    void toEntity_ShouldMapRequestToTransaction() {

        UUID customerId = UUID.randomUUID();

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setCustomerId(customerId);
        request.setAmount(BigDecimal.valueOf(1500));

        Transaction transaction = mapper.toEntity(request);

        assertNotNull(transaction);
        assertNull(transaction.getId());

        assertEquals(customerId, transaction.getCustomerId());
        assertEquals(BigDecimal.valueOf(1500), transaction.getAmount());
        assertEquals("PENDING", transaction.getStatus());

        assertNotNull(transaction.getCreatedAt());

        // createdAt should be very close to "now"
        assertTrue(
                transaction.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void toResponse_ShouldMapTransactionToResponse() {

        UUID transactionId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .customerId(customerId)
                .amount(BigDecimal.valueOf(2500))
                .status("APPROVED")
                .createdAt(createdAt)
                .build();

        TransactionResponse response = mapper.toResponse(transaction);

        assertNotNull(response);

        assertEquals(transactionId, response.getId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals(BigDecimal.valueOf(2500), response.getAmount());
        assertEquals("APPROVED", response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
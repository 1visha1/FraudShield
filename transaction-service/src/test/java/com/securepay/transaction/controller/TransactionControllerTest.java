package com.securepay.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securepay.transaction.dto.CreateTransactionRequest;
import com.securepay.transaction.entity.Transaction;
import com.securepay.transaction.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create transaction successfully")
    void create_ShouldReturnTransaction() throws Exception {

        UUID transactionId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setCustomerId(customerId);
        request.setAmount(BigDecimal.valueOf(1000));

        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .customerId(customerId)
                .amount(BigDecimal.valueOf(1000))
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionService.create(any(CreateTransactionRequest.class)))
                .thenReturn(transaction);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should return Bad Request when request is invalid")
    void create_ShouldReturnBadRequest_WhenValidationFails() throws Exception {

        CreateTransactionRequest request = new CreateTransactionRequest();

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
package com.fraudshield.transaction.controller;

import com.fraudshield.transaction.dto.CreateTransactionRequest;
import com.fraudshield.transaction.entity.Transaction;
import com.fraudshield.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    public Transaction create(
            @Valid
            @RequestBody
            CreateTransactionRequest request) {
        log.warn("transaction: {}",request);
        return service.create(request);
    }
}
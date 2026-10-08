package com.fraudshield.transaction.controller;

import com.fraudshield.common.util.ApiResponse;
import com.fraudshield.transaction.dto.CreateTransactionRequest;
import com.fraudshield.transaction.dto.response.TransactionResponse;
import com.fraudshield.transaction.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<TransactionResponse>>> create(
            @Valid @RequestBody CreateTransactionRequest request, HttpServletRequest httpServletRequest) {
        return service.create(request)
                .map(transactionResponse -> {
                    ApiResponse<TransactionResponse> response = ApiResponse.<TransactionResponse>builder()
                            .success(true)
                            .message("Transaction created successfully")
                            .data(transactionResponse)
                            .path(httpServletRequest.getRequestURI())
                            .build();
                    return new ResponseEntity<>(response, HttpStatus.CREATED);
                });
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @PathVariable UUID id, HttpServletRequest httpServletRequest) {
        TransactionResponse transactionResponse = service.getTransactionById(id);
        ApiResponse<TransactionResponse> response = ApiResponse.<TransactionResponse>builder()
                .success(true)
                .message("Transaction fetched successfully")
                .data(transactionResponse)
                .path(httpServletRequest.getRequestURI())
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
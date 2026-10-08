package com.fraudshield.transaction.service;

import com.fraudshield.transaction.dto.CreateTransactionRequest;
import com.fraudshield.transaction.entity.Transaction;
import com.fraudshield.transaction.event.AuthChallengeCompletedEvent;
import com.fraudshield.transaction.event.TransactionApprovedEvent;
import com.fraudshield.transaction.event.TransactionBlockedEvent;
import com.fraudshield.transaction.event.TransactionCreatedEvent;
import com.fraudshield.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.fraudshield.transaction.config.RabbitMQConfig.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public Transaction create(CreateTransactionRequest request) {
        log.info("Creating new transaction for customer: {} with amount: {}", request.getCustomerId(), request.getAmount());

        Transaction transaction = Transaction.builder()
                        .customerId(request.getCustomerId())
                        .amount(request.getAmount())
                        .status("PENDING")
                        .createdAt(LocalDateTime.now())
                        .build();

        Transaction saved = repository.save(transaction);

        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
                        .eventType("TRANSACTION_CREATED")
                        .transactionId(saved.getId())
                        .customerId(saved.getCustomerId())
                        .amount(saved.getAmount())
                        .build();

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
        log.info("Published TransactionCreatedEvent for transaction: {}", saved.getId());

        return saved;
    }

    @Transactional
    public void approveTransaction(AuthChallengeCompletedEvent challengeEvent) {
        UUID txnId = challengeEvent.getTransactionId();
        log.info("Attempting to approve transaction: {}", txnId);

        repository.findById(txnId).ifPresentOrElse(transaction -> {
            if (!"PENDING".equals(transaction.getStatus())) {
                log.warn("Transaction {} is in status {} and cannot be approved.", txnId, transaction.getStatus());
                return;
            }

            transaction.setStatus("APPROVED");
            repository.save(transaction);
            log.info("Transaction {} status updated to APPROVED", txnId);

            TransactionApprovedEvent approvedEvent = TransactionApprovedEvent.builder()
                    .transactionId(txnId)
                    .customerId(transaction.getCustomerId())
                    .amount(transaction.getAmount())
                    .approvedAt(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(EXCHANGE, "transaction.approved", approvedEvent);
            log.info("Published TransactionApprovedEvent for transaction: {}", txnId);

        }, () -> log.error("Transaction not found: {}", txnId));
    }

    @Transactional
    public void blockTransaction(com.fraudshield.transaction.event.TransactionBlockedEvent blockEvent) {
        UUID txnId = blockEvent.getTransactionId();
        log.info("Attempting to block transaction: {}", txnId);

        repository.findById(txnId).ifPresentOrElse(transaction -> {
            transaction.setStatus("BLOCK");
            repository.save(transaction);
            log.info("Transaction {} status updated to BLOCK", txnId);

            // Audit already handled by the incoming event in audit-service
        }, () -> log.error("Transaction not found: {}", txnId));
    }
}
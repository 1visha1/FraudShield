package com.fraudshield.transaction.consumer;

import com.fraudshield.transaction.event.AuthChallengeCompletedEvent;
import com.fraudshield.transaction.event.TransactionBlockedEvent;
import com.fraudshield.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionStatusConsumerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionStatusConsumer consumer;

    @Test
    void consumeApproval_ShouldCallApproveTransaction() {

        UUID transactionId = UUID.randomUUID();

        AuthChallengeCompletedEvent event = AuthChallengeCompletedEvent.builder()
                .transactionId(transactionId)
                .customerId(UUID.randomUUID())
                .authSessionId(UUID.randomUUID())
                .verified(true)
                .verifiedAt(LocalDateTime.now())
                .build();

        consumer.consumeApproval(event);

        verify(transactionService, times(1))
                .approveTransaction(event);
    }

    @Test
    void consumeBlock_ShouldCallBlockTransaction() {

        UUID transactionId = UUID.randomUUID();

        TransactionBlockedEvent event = TransactionBlockedEvent.builder()
                .transactionId(transactionId)
                .customerId(UUID.randomUUID())
                .reason("Fraud detected")
                .blockedAt(LocalDateTime.now())
                .build();

        consumer.consumeBlock(event);

        verify(transactionService, times(1))
                .blockTransaction(event);
    }
}
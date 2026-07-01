package com.securepay.transaction.service;

import com.securepay.transaction.dto.CreateTransactionRequest;
import com.securepay.transaction.entity.Transaction;
import com.securepay.transaction.event.AuthChallengeCompletedEvent;
import com.securepay.transaction.event.TransactionApprovedEvent;
import com.securepay.transaction.event.TransactionBlockedEvent;
import com.securepay.transaction.event.TransactionCreatedEvent;
import com.securepay.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.securepay.transaction.config.RabbitMQConfig.EXCHANGE;
import static com.securepay.transaction.config.RabbitMQConfig.ROUTING_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void create_ShouldSaveTransaction_AndPublishEvent() {

        UUID customerId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setCustomerId(customerId);
        request.setAmount(BigDecimal.valueOf(500));

        Transaction saved = Transaction.builder()
                .id(transactionId)
                .customerId(customerId)
                .amount(BigDecimal.valueOf(500))
                .status("PENDING")
                .build();

        when(repository.save(any(Transaction.class))).thenReturn(saved);

        Transaction result = transactionService.create(request);

        assertNotNull(result);
        assertEquals(transactionId, result.getId());
        assertEquals(customerId, result.getCustomerId());
        assertEquals(BigDecimal.valueOf(500), result.getAmount());
        assertEquals("PENDING", result.getStatus());

        verify(repository, times(1)).save(any(Transaction.class));

        ArgumentCaptor<TransactionCreatedEvent> captor =
                ArgumentCaptor.forClass(TransactionCreatedEvent.class);

        verify(rabbitTemplate, times(1))
                .convertAndSend(eq(EXCHANGE), eq(ROUTING_KEY), captor.capture());

        TransactionCreatedEvent event = captor.getValue();

        assertEquals(transactionId, event.getTransactionId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(BigDecimal.valueOf(500), event.getAmount());
        assertEquals("TRANSACTION_CREATED", event.getEventType());
    }

    @Test
    void approveTransaction_ShouldApprove_WhenPending() {

        UUID txnId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .id(txnId)
                .customerId(customerId)
                .amount(BigDecimal.valueOf(1000))
                .status("PENDING")
                .build();

        AuthChallengeCompletedEvent event = AuthChallengeCompletedEvent.builder()
                .transactionId(txnId)
                .verified(true)
                .build();

        when(repository.findById(txnId))
                .thenReturn(Optional.of(transaction));

        transactionService.approveTransaction(event);

        assertEquals("APPROVED", transaction.getStatus());

        verify(repository).save(transaction);

        verify(rabbitTemplate)
                .convertAndSend(
                        eq(EXCHANGE),
                        eq("transaction.approved"),
                        isA(TransactionApprovedEvent.class)
                );
    }

    @Test
    void approveTransaction_ShouldNotApprove_WhenAlreadyApproved() {

        UUID txnId = UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .id(txnId)
                .status("APPROVED")
                .build();

        AuthChallengeCompletedEvent event = AuthChallengeCompletedEvent.builder()
                .transactionId(txnId)
                .build();

        when(repository.findById(txnId))
                .thenReturn(Optional.of(transaction));

        transactionService.approveTransaction(event);

        verify(repository, never()).save(any(Transaction.class));

        verify(rabbitTemplate, never())
                .convertAndSend(
                        eq(EXCHANGE),
                        eq("transaction.approved"),
                        isA(TransactionApprovedEvent.class)
                );
    }

    @Test
    void approveTransaction_ShouldDoNothing_WhenTransactionNotFound() {

        UUID txnId = UUID.randomUUID();

        AuthChallengeCompletedEvent event = AuthChallengeCompletedEvent.builder()
                .transactionId(txnId)
                .build();

        when(repository.findById(txnId))
                .thenReturn(Optional.empty());

        transactionService.approveTransaction(event);

        verify(repository, never()).save(any(Transaction.class));

        verify(rabbitTemplate, never())
                .convertAndSend(
                        eq(EXCHANGE),
                        eq("transaction.approved"),
                        isA(TransactionApprovedEvent.class)
                );
    }

    @Test
    void blockTransaction_ShouldUpdateStatusToBlock() {

        UUID txnId = UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .id(txnId)
                .status("PENDING")
                .build();

        TransactionBlockedEvent event = TransactionBlockedEvent.builder()
                .transactionId(txnId)
                .reason("Fraud")
                .build();

        when(repository.findById(txnId))
                .thenReturn(Optional.of(transaction));

        transactionService.blockTransaction(event);

        assertEquals("BLOCK", transaction.getStatus());

        verify(repository).save(transaction);
    }

    @Test
    void blockTransaction_ShouldDoNothing_WhenTransactionNotFound() {

        UUID txnId = UUID.randomUUID();

        TransactionBlockedEvent event = TransactionBlockedEvent.builder()
                .transactionId(txnId)
                .build();

        when(repository.findById(txnId))
                .thenReturn(Optional.empty());

        transactionService.blockTransaction(event);

        verify(repository, never()).save(any(Transaction.class));
    }
}
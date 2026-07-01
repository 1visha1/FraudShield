package com.securepay.transaction.consumer;

import com.securepay.transaction.event.AuthChallengeCompletedEvent;
import com.securepay.transaction.service.TransactionService;
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
class ChallengeCompletedConsumerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ChallengeCompletedConsumer consumer;

    @Test
    void consume_ShouldCallApproveTransaction() {

        UUID transactionId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID authSessionId = UUID.randomUUID();

        AuthChallengeCompletedEvent event = AuthChallengeCompletedEvent.builder()
                .transactionId(transactionId)
                .customerId(customerId)
                .authSessionId(authSessionId)
                .verified(true)
                .verifiedAt(LocalDateTime.now())
                .build();

        consumer.consume(event);

        verify(transactionService, times(1)).approveTransaction(event);
    }
}
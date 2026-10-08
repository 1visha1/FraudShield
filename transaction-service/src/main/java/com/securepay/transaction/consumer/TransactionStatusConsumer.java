package com.fraudshield.transaction.consumer;

import com.fraudshield.transaction.config.RabbitMQConfig;
import com.fraudshield.transaction.event.AuthChallengeCompletedEvent;
import com.fraudshield.transaction.event.TransactionBlockedEvent;
import com.fraudshield.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionStatusConsumer {

    private final TransactionService transactionService;

    @RabbitListener(queues = RabbitMQConfig.CHALLENGE_COMPLETED_QUEUE)
    public void consumeApproval(AuthChallengeCompletedEvent event) {
        log.info("Received AuthChallengeCompletedEvent for transaction: {}", event.getTransactionId());
        transactionService.approveTransaction(event);
    }

    @RabbitListener(queues = RabbitMQConfig.BLOCKED_QUEUE)
    public void consumeBlock(TransactionBlockedEvent event) {
        log.info("Received TransactionBlockedEvent for transaction: {}", event.getTransactionId());
        transactionService.blockTransaction(event);
    }
}
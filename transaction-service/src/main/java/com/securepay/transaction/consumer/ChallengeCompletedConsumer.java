package com.securepay.transaction.consumer;

import com.securepay.transaction.config.RabbitMQConfig;
import com.securepay.transaction.event.AuthChallengeCompletedEvent;
import com.securepay.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChallengeCompletedConsumer {

    private final TransactionService transactionService;

    @RabbitListener(queues = RabbitMQConfig.CHALLENGE_COMPLETED_QUEUE)
    public void consume(AuthChallengeCompletedEvent event) {
        log.info("Received AuthChallengeCompletedEvent for transaction: {}", event.getTransactionId());
        transactionService.approveTransaction(event);
    }
}
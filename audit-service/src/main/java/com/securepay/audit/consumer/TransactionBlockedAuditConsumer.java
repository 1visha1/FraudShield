package com.securepay.audit.consumer;

import com.securepay.audit.event.TransactionBlockedEvent;
import com.securepay.audit.service.AuditWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionBlockedAuditConsumer {

    private final AuditWriter writer;

    @RabbitListener(queues = "audit.transaction.blocked.q")
    public void consume(TransactionBlockedEvent event) {
        log.info("Auditing TransactionBlockedEvent for transaction: {}", event.getTransactionId());
        writer.write("TRANSACTION_BLOCKED", "transaction-service", event);
    }
}
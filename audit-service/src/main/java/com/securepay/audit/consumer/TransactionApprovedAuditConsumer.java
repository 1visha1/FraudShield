package com.securepay.audit.consumer;

import com.securepay.audit.event.TransactionApprovedEvent;
import com.securepay.audit.service.AuditWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionApprovedAuditConsumer {

    private final AuditWriter writer;

    @RabbitListener(queues = "audit.transaction.approved.q")
    public void consume(TransactionApprovedEvent event) {
        log.info("Auditing TransactionApprovedEvent for transaction: {}", event.getTransactionId());
        writer.write("TRANSACTION_APPROVED", "transaction-service", event);
    }
}
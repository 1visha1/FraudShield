package com.securepay.audit.consumer;

import com.securepay.audit.event.RiskAssessedEvent;
import com.securepay.audit.service.AuditWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RiskAuditConsumer {

    private final AuditWriter writer;

    @RabbitListener(queues = "risk.assessed.q")
    public void consume(RiskAssessedEvent event) {
        log.info("Auditing RiskAssessedEvent for transaction: {}, score: {}",
                event.getTransactionId(), event.getRiskScore());
        
        try {
            writer.write("RISK_ASSESSED", "risk-engine", event);
            log.debug("Successfully wrote audit record for RISK_ASSESSED: {}", event.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to audit RiskAssessedEvent for txn: {}. Error: {}", 
                    event.getTransactionId(), e.getMessage());
        }
    }
}
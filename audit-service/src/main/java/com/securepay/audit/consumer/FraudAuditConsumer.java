package com.securepay.audit.consumer;

import com.securepay.audit.event.FraudDetectedEvent;
import com.securepay.audit.service.AuditWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FraudAuditConsumer {

    private final AuditWriter writer;

    @RabbitListener(queues = "fraud.detected.q")
    public void consume(FraudDetectedEvent event) {
        writer.write("FRAUD_DETECTED", "fraud-engine", event);
    }
}
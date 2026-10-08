package com.fraudshield.audit.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudshield.audit.service.AuditStreamService;
import com.fraudshield.audit.model.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventConsumer {

    private final AuditStreamService streamService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "audit.q")
    public void consume(Message message) {
        try {
            String payload = new String(message.getBody());
            JsonNode eventNode = objectMapper.readTree(payload);
            
            String eventType = eventNode.has("eventType") ? eventNode.get("eventType").asText() : "UNKNOWN_EVENT";
            String transactionId = eventNode.has("transactionId") ? eventNode.get("transactionId").asText() : null;
            String serviceName = eventNode.has("serviceName") ? eventNode.get("serviceName").asText() : "unknown-service";

            AuditEvent event = AuditEvent.builder()
                    .eventType(eventType)
                    .transactionId(transactionId)
                    .serviceName(serviceName)
                    .payload(payload)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("Received audit event of type: {}", eventType);
            streamService.publish(event);

        } catch (Exception e) {
            log.error("Error processing audit event", e);
        }
    }
}
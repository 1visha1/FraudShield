package com.securepay.audit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securepay.audit.model.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditWriter {

    private final ObjectMapper mapper;
    private final AuditStreamService streamService;

    public void write(String eventType, String service, Object payload) {
        try {
            // Convert payload to JsonNode to safely extract transactionId
            JsonNode node = mapper.valueToTree(payload);
            String txnId = node.has("transactionId") ? node.get("transactionId").asText() : null;

            AuditEvent event = AuditEvent.builder()
                    .eventType(eventType)
                    .serviceName(service)
                    .transactionId(txnId)
                    .payload(mapper.writeValueAsString(payload))
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("Preparing to stream AuditEvent: {}", event);
            streamService.publish(event);

        } catch (Exception e) {
            log.error("Failed to process audit event: {}", eventType, e);
            throw new RuntimeException("Audit write failed", e);
        }
    }
}
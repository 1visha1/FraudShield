package com.securepay.audit.service;

import com.securepay.audit.model.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditStreamService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(AuditEvent event) {
        log.info("Publishing to Redis Stream: {}", event);
        try {
            Map<String, String> values = new HashMap<>();

            // Fix: Adding all fields to the Redis Stream Map
            values.put("eventType", event.getEventType() != null ? event.getEventType() : "");
            values.put("serviceName", event.getServiceName() != null ? event.getServiceName() : "");
            values.put("transactionId", event.getTransactionId() != null ? event.getTransactionId() : "");
            values.put("payload", event.getPayload() != null ? event.getPayload() : "{}");
            values.put("timestamp", event.getTimestamp() != null ? event.getTimestamp().toString() : "");

            RecordId id = redisTemplate.opsForStream()
                    .add("audit-stream", values);

            log.info("Redis Stream successfully published. Record ID: {}, Event: {}", id, event.getEventType());

        } catch (Exception ex) {
            log.error("Redis Stream publish failed", ex);
        }
    }
}
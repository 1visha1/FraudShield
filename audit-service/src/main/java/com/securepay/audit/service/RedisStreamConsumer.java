package com.securepay.audit.service;

import com.securepay.audit.model.AuditEvent;
import com.securepay.audit.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final AuditEventRepository auditEventRepository;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        log.info("Received event from Redis Stream: {}", message.getValue());
        
        AuditEvent.AuditEventBuilder builder = AuditEvent.builder();
        
        if (message.getValue().containsKey("event")) {
            builder.eventData(message.getValue().get("event"));
        } else {
            builder.eventType(message.getValue().get("eventType"))
                   .serviceName(message.getValue().get("serviceName"))
                   .transactionId(message.getValue().get("transactionId"))
                   .payload(message.getValue().get("payload"));
            
            String ts = message.getValue().get("timestamp");
            if (ts != null && !ts.isEmpty()) {
                try {
                    builder.timestamp(java.time.LocalDateTime.parse(ts));
                } catch (Exception ex) {
                    log.error("Failed to parse stream timestamp: {}", ts, ex);
                }
            }
        }
        
        auditEventRepository.save(builder.build());
    }
}
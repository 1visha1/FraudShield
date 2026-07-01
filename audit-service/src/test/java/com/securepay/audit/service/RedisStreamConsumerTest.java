package com.securepay.audit.service;

import com.securepay.audit.model.AuditEvent;
import com.securepay.audit.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisStreamConsumerTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @InjectMocks
    private RedisStreamConsumer redisStreamConsumer;

    @Test
    void onMessage_ShouldSaveEvent_WhenEventFieldExists() {

        Map<String, String> values = new HashMap<>();
        values.put("event", "{\"type\":\"PAYMENT\"}");

        MapRecord<String, String, String> record =
                MapRecord.create("audit-stream", values);

        redisStreamConsumer.onMessage(record);

        ArgumentCaptor<AuditEvent> captor =
                ArgumentCaptor.forClass(AuditEvent.class);

        verify(auditEventRepository).save(captor.capture());

        AuditEvent saved = captor.getValue();

        assertEquals("{\"type\":\"PAYMENT\"}", saved.getEventData());
    }

    @Test
    void onMessage_ShouldSaveAuditEvent_WhenStructuredFieldsExist() {

        Map<String, String> values = new HashMap<>();
        values.put("eventType", "PAYMENT");
        values.put("serviceName", "payment-service");
        values.put("transactionId", "TXN123");
        values.put("payload", "{\"amount\":100}");
        values.put("timestamp", "2024-01-01T10:15:30");

        MapRecord<String, String, String> record =
                MapRecord.create("audit-stream", values);

        redisStreamConsumer.onMessage(record);

        ArgumentCaptor<AuditEvent> captor =
                ArgumentCaptor.forClass(AuditEvent.class);

        verify(auditEventRepository).save(captor.capture());

        AuditEvent saved = captor.getValue();

        assertEquals("PAYMENT", saved.getEventType());
        assertEquals("payment-service", saved.getServiceName());
        assertEquals("TXN123", saved.getTransactionId());
        assertEquals("{\"amount\":100}", saved.getPayload());
        assertEquals(LocalDateTime.parse("2024-01-01T10:15:30"), saved.getTimestamp());
    }

    @Test
    void onMessage_ShouldHandleInvalidTimestamp() {

        Map<String, String> values = new HashMap<>();
        values.put("eventType", "PAYMENT");
        values.put("serviceName", "payment-service");
        values.put("transactionId", "TXN123");
        values.put("payload", "{}");
        values.put("timestamp", "invalid-date");

        MapRecord<String, String, String> record =
                MapRecord.create("audit-stream", values);

        redisStreamConsumer.onMessage(record);

        ArgumentCaptor<AuditEvent> captor =
                ArgumentCaptor.forClass(AuditEvent.class);

        verify(auditEventRepository).save(captor.capture());

        AuditEvent saved = captor.getValue();

        assertNull(saved.getTimestamp());
        assertEquals("PAYMENT", saved.getEventType());
    }

    @Test
    void onMessage_ShouldHandleMissingTimestamp() {

        Map<String, String> values = new HashMap<>();
        values.put("eventType", "LOGIN");
        values.put("serviceName", "auth-service");
        values.put("transactionId", "TXN999");
        values.put("payload", "{}");

        MapRecord<String, String, String> record =
                MapRecord.create("audit-stream", values);

        redisStreamConsumer.onMessage(record);

        ArgumentCaptor<AuditEvent> captor =
                ArgumentCaptor.forClass(AuditEvent.class);

        verify(auditEventRepository).save(captor.capture());

        AuditEvent saved = captor.getValue();

        assertNull(saved.getTimestamp());
        assertEquals("LOGIN", saved.getEventType());
    }

    @Test
    void onMessage_ShouldHandleEmptyTimestamp() {

        Map<String, String> values = new HashMap<>();
        values.put("eventType", "LOGIN");
        values.put("serviceName", "auth-service");
        values.put("transactionId", "TXN999");
        values.put("payload", "{}");
        values.put("timestamp", "");

        MapRecord<String, String, String> record =
                MapRecord.create("audit-stream", values);

        redisStreamConsumer.onMessage(record);

        verify(auditEventRepository).save(any(AuditEvent.class));
    }
}
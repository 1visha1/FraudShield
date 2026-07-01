package com.securepay.audit.service;

import com.securepay.audit.model.AuditEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditStreamServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @InjectMocks
    private AuditStreamService service;

    private AuditEvent event;

    @BeforeEach
    void setUp() {

        event = AuditEvent.builder()
                .eventType("PAYMENT")
                .serviceName("payment-service")
                .transactionId("TXN123")
                .payload("{\"amount\":100}")
                .timestamp(LocalDateTime.of(2024, 1, 1, 10, 15, 30))
                .build();

        when(redisTemplate.opsForStream())
                .thenReturn(streamOperations);
    }

    @Test
    void publish_ShouldPublishEventSuccessfully() {

        RecordId recordId = RecordId.of("1-0");

        when(streamOperations.add(anyString(), anyMap()))
                .thenReturn(recordId);

        service.publish(event);

        ArgumentCaptor<Map<String, String>> captor =
                ArgumentCaptor.forClass(Map.class);

        verify(streamOperations).add(
                eq("audit-stream"),
                captor.capture());

        Map<String, String> values = captor.getValue();

        assertEquals("PAYMENT", values.get("eventType"));
        assertEquals("payment-service", values.get("serviceName"));
        assertEquals("TXN123", values.get("transactionId"));
        assertEquals("{\"amount\":100}", values.get("payload"));
        assertEquals("2024-01-01T10:15:30", values.get("timestamp"));
    }

    @Test
    void publish_ShouldHandleNullFields() {

        AuditEvent event = AuditEvent.builder().build();

        when(streamOperations.add(anyString(), anyMap()))
                .thenReturn(RecordId.of("2-0"));

        service.publish(event);

        ArgumentCaptor<Map<String, String>> captor =
                ArgumentCaptor.forClass(Map.class);

        verify(streamOperations).add(
                eq("audit-stream"),
                captor.capture());

        Map<String, String> values = captor.getValue();

        assertEquals("", values.get("eventType"));
        assertEquals("", values.get("serviceName"));
        assertEquals("", values.get("transactionId"));
        assertEquals("{}", values.get("payload"));
        assertEquals("", values.get("timestamp"));
    }

    @Test
    void publish_ShouldHandleExceptionGracefully() {

        when(streamOperations.add(anyString(), anyMap()))
                .thenThrow(new RuntimeException("Redis Error"));

        assertDoesNotThrow(() -> service.publish(event));

        verify(streamOperations)
                .add(eq("audit-stream"), anyMap());
    }

    @Test
    void publish_ShouldCallOpsForStream() {

        when(streamOperations.add(anyString(), anyMap()))
                .thenReturn(RecordId.of("3-0"));

        service.publish(event);

        verify(redisTemplate).opsForStream();
    }

    @Test
    void publish_ShouldPublishOnlyOnce() {

        when(streamOperations.add(anyString(), anyMap()))
                .thenReturn(RecordId.of("4-0"));

        service.publish(event);

        verify(streamOperations, times(1))
                .add(anyString(), anyMap());

        verifyNoMoreInteractions(streamOperations);
    }
}
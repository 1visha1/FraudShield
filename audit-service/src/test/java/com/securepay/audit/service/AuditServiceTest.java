package com.fraudshield.audit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @SuppressWarnings("rawtypes")
    @Mock
    private StreamOperations streamOperations;

    @InjectMocks
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
    }

    @Test
    void auditEvent_ShouldPublishEventToRedisStream() {

        String event = "{\"event\":\"PAYMENT_SUCCESS\"}";

        auditService.auditEvent(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> captor =
                ArgumentCaptor.forClass(Map.class);

        verify(streamOperations).add(
                eq("audit-stream"),
                captor.capture());

        Map<String, String> values = captor.getValue();

        assertEquals(event, values.get("event"));
    }

    @Test
    void auditEvent_ShouldHandleEmptyEvent() {

        auditService.auditEvent("");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> captor =
                ArgumentCaptor.forClass(Map.class);

        verify(streamOperations).add(
                eq("audit-stream"),
                captor.capture());

        assertEquals("", captor.getValue().get("event"));
    }

    @Test
    void auditEvent_ShouldHandleNullEvent() {

        auditService.auditEvent(null);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> captor =
                ArgumentCaptor.forClass(Map.class);

        verify(streamOperations).add(
                eq("audit-stream"),
                captor.capture());

        assertNull(captor.getValue().get("event"));
    }

    @Test
    void auditEvent_ShouldCallOpsForStream() {

        auditService.auditEvent("TEST");

        verify(redisTemplate).opsForStream();
    }

    @Test
    void auditEvent_ShouldThrowException_WhenRedisFails() {

        doThrow(new RuntimeException("Redis Error"))
                .when(streamOperations)
                .add(anyString(), anyMap());

        assertThrows(RuntimeException.class,
                () -> auditService.auditEvent("TEST"));

        verify(streamOperations)
                .add(anyString(), anyMap());
    }

    @Test
    void auditEvent_ShouldPublishOnlyOnce() {

        auditService.auditEvent("HELLO");

        verify(streamOperations, times(1))
                .add(anyString(), anyMap());

        verifyNoMoreInteractions(streamOperations);
    }
}
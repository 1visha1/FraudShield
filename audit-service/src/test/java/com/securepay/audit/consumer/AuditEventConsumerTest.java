package com.securepay.audit.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securepay.audit.model.AuditEvent;
import com.securepay.audit.service.AuditStreamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditEventConsumerTest {

    @Mock
    private AuditStreamService streamService;

    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditEventConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        consumer = new AuditEventConsumer(streamService, objectMapper);
    }

    @Test
    void consume_ShouldPublishAuditEvent() {

        String json = """
                {
                  "eventType":"PAYMENT",
                  "transactionId":"TXN123",
                  "serviceName":"payment-service"
                }
                """;

        Message message = new Message(json.getBytes());

        consumer.consume(message);

        ArgumentCaptor<AuditEvent> captor =
                ArgumentCaptor.forClass(AuditEvent.class);

        verify(streamService).publish(captor.capture());

        AuditEvent event = captor.getValue();

        assertEquals("PAYMENT", event.getEventType());
        assertEquals("TXN123", event.getTransactionId());
        assertEquals("payment-service", event.getServiceName());
        assertEquals(json, event.getPayload());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void consume_ShouldUseDefaultEventType_WhenMissing() {

        String json = """
                {
                  "transactionId":"TXN123",
                  "serviceName":"payment-service"
                }
                """;

        Message message = new Message(json.getBytes());

        consumer.consume(message);

        ArgumentCaptor<AuditEvent> captor =
                ArgumentCaptor.forClass(AuditEvent.class);

        verify(streamService).publish(captor.capture());

        assertEquals(
                "UNKNOWN_EVENT",
                captor.getValue().getEventType());
    }

    @Test
    void consume_ShouldUseDefaultServiceName_WhenMissing() {

        String json = """
                {
                  "eventType":"LOGIN",
                  "transactionId":"TXN999"
                }
                """;

        Message message = new Message(json.getBytes());

        consumer.consume(message);

        ArgumentCaptor<AuditEvent> captor =
                ArgumentCaptor.forClass(AuditEvent.class);

        verify(streamService).publish(captor.capture());

        assertEquals(
                "unknown-service",
                captor.getValue().getServiceName());
    }

    @Test
    void consume_ShouldHandleMissingTransactionId() {

        String json = """
                {
                  "eventType":"LOGIN",
                  "serviceName":"auth-service"
                }
                """;

        Message message = new Message(json.getBytes());

        consumer.consume(message);

        ArgumentCaptor<AuditEvent> captor =
                ArgumentCaptor.forClass(AuditEvent.class);

        verify(streamService).publish(captor.capture());

        assertNull(captor.getValue().getTransactionId());
    }

    @Test
    void consume_ShouldHandleInvalidJson() {

        String json = "{invalid json}";

        Message message = new Message(json.getBytes());

        assertDoesNotThrow(() -> consumer.consume(message));

        verify(streamService, never()).publish(any());
    }

    @Test
    void consume_ShouldHandleExceptionThrownByPublish() {

        String json = """
                {
                  "eventType":"PAYMENT"
                }
                """;

        Message message = new Message(json.getBytes());

        doThrow(new RuntimeException("Redis Down"))
                .when(streamService)
                .publish(any(AuditEvent.class));

        assertDoesNotThrow(() -> consumer.consume(message));

        verify(streamService).publish(any(AuditEvent.class));
    }

    @Test
    void consume_ShouldHandleEmptyJson() {

        String json = "{}";

        Message message = new Message(json.getBytes());

        consumer.consume(message);

        ArgumentCaptor<AuditEvent> captor =
                ArgumentCaptor.forClass(AuditEvent.class);

        verify(streamService).publish(captor.capture());

        AuditEvent event = captor.getValue();

        assertEquals("UNKNOWN_EVENT", event.getEventType());
        assertEquals("unknown-service", event.getServiceName());
        assertNull(event.getTransactionId());
    }
}
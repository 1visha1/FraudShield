package com.securepay.transaction.service;

import com.securepay.transaction.entity.OutboxEvent;
import com.securepay.transaction.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.securepay.transaction.config.RabbitMQConfig.EXCHANGE;
import static com.securepay.transaction.config.RabbitMQConfig.ROUTING_KEY;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OutboxEventPublisher outboxEventPublisher;

    @Test
    void publishEvents_ShouldReturn_WhenNoEventsExist() {

        when(outboxEventRepository.findByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(Collections.emptyList());

        outboxEventPublisher.publishEvents();

        verify(outboxEventRepository, times(1))
                .findByPublishedFalseOrderByCreatedAtAsc();

        verifyNoInteractions(rabbitTemplate);

        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void publishEvents_ShouldPublishAndSaveEvent() {

        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setPayload("{\"event\":\"transaction-created\"}");
        event.setPublished(false);
        event.setCreatedAt(LocalDateTime.now());

        when(outboxEventRepository.findByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(List.of(event));

        outboxEventPublisher.publishEvents();

        verify(rabbitTemplate, times(1))
                .convertAndSend(
                        eq(EXCHANGE),
                        eq(ROUTING_KEY),
                        eq(event.getPayload())
                );

        assertTrue(event.isPublished());

        verify(outboxEventRepository, times(1))
                .save(event);
    }

    @Test
    void publishEvents_ShouldNotSave_WhenRabbitThrowsException() {

        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setPayload("{\"event\":\"transaction-created\"}");
        event.setPublished(false);
        event.setCreatedAt(LocalDateTime.now());

        when(outboxEventRepository.findByPublishedFalseOrderByCreatedAtAsc())
                .thenReturn(List.of(event));

        doThrow(new RuntimeException("RabbitMQ Down"))
                .when(rabbitTemplate)
                .convertAndSend(
                        eq(EXCHANGE),
                        eq(ROUTING_KEY),
                        eq(event.getPayload())
                );

        outboxEventPublisher.publishEvents();

        verify(rabbitTemplate, times(1))
                .convertAndSend(
                        eq(EXCHANGE),
                        eq(ROUTING_KEY),
                        eq(event.getPayload())
                );

        verify(outboxEventRepository, never()).save(any());

        assertTrue(!event.isPublished());
    }
}
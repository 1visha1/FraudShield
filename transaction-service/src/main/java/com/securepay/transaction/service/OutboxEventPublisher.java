package com.fraudshield.transaction.service;

import com.fraudshield.transaction.entity.OutboxEvent;
import com.fraudshield.transaction.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fraudshield.transaction.config.RabbitMQConfig.EXCHANGE;
import static com.fraudshield.transaction.config.RabbitMQConfig.ROUTING_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 10000) // Poll every 10 seconds
    @Transactional
    public void publishEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByPublishedFalseOrderByCreatedAtAsc();
        if (events.isEmpty()) {
            return;
        }

        log.info("Found {} unpublished events. Attempting to publish.", events.size());

        for (OutboxEvent event : events) {
            try {
                // We need to configure publisher confirms to do this safely
                rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event.getPayload());
                event.setPublished(true);
                outboxEventRepository.save(event);
                log.info("Successfully published event with ID: {}", event.getId());
            } catch (Exception e) {
                log.error("Failed to publish event with ID: {}. It will be retried.", event.getId(), e);
                // The transaction will roll back, and the event will be picked up in the next run.
            }
        }
    }
}
package com.fraudshield.audit.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RabbitMQConfigTest {

    private RabbitMQConfig rabbitMQConfig;

    @BeforeEach
    void setUp() {
        rabbitMQConfig = new RabbitMQConfig();
    }

    @Test
    void testTopicExchange() {
        TopicExchange exchange = rabbitMQConfig.topicExchange();

        assertNotNull(exchange);
        assertEquals(RabbitMQConfig.EXCHANGE, exchange.getName());
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
    }

    @Test
    void testAuditQueue() {
        Queue queue = rabbitMQConfig.auditQueue();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.AUDIT_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void testAuditBinding() {
        Queue queue = rabbitMQConfig.auditQueue();
        TopicExchange exchange = rabbitMQConfig.topicExchange();

        Binding binding = rabbitMQConfig.auditBinding(queue, exchange);

        assertNotNull(binding);
        assertEquals(queue.getName(), binding.getDestination());
        assertEquals(exchange.getName(), binding.getExchange());
        assertEquals("#", binding.getRoutingKey());
        assertEquals(Binding.DestinationType.QUEUE, binding.getDestinationType());
    }

    @Test
    void testRabbitTemplate() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);

        RabbitTemplate rabbitTemplate = rabbitMQConfig.rabbitTemplate(connectionFactory);

        assertNotNull(rabbitTemplate);
        assertSame(connectionFactory, rabbitTemplate.getConnectionFactory());
    }
}
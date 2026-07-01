package com.securepay.device.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RabbitMQConfigTest {

    private RabbitMQConfig config;

    @BeforeEach
    void setUp() {
        config = new RabbitMQConfig();
    }

    @Test
    void shouldCreateExchange() {

        TopicExchange exchange = config.exchange();

        assertNotNull(exchange);
        assertEquals(RabbitMQConfig.EXCHANGE, exchange.getName());
        assertTrue(exchange.isDurable());
    }

    @Test
    void shouldCreateQueue() {

        Queue queue = config.queue();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.QUEUE, queue.getName());
        assertTrue(queue.isDurable());

        assertEquals("", queue.getArguments().get("x-dead-letter-exchange"));
        assertEquals(
                RabbitMQConfig.RETRY_QUEUE,
                queue.getArguments().get("x-dead-letter-routing-key"));
    }

    @Test
    void shouldCreateRetryQueue() {

        Queue queue = config.retryQueue();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.RETRY_QUEUE, queue.getName());

        assertEquals("", queue.getArguments().get("x-dead-letter-exchange"));
        assertEquals(
                RabbitMQConfig.QUEUE,
                queue.getArguments().get("x-dead-letter-routing-key"));
        assertEquals(
                5000,
                queue.getArguments().get("x-message-ttl"));
    }

    @Test
    void shouldCreateDeadLetterQueue() {

        Queue queue = config.dlq();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.DLQ, queue.getName());
    }

    @Test
    void shouldCreateBinding() {

        Binding binding = config.binding();

        assertNotNull(binding);
        assertEquals(
                RabbitMQConfig.QUEUE,
                binding.getDestination());
        assertEquals(
                RabbitMQConfig.EXCHANGE,
                binding.getExchange());
        assertEquals(
                RabbitMQConfig.ROUTING_KEY,
                binding.getRoutingKey());
    }

    @Test
    void shouldCreateDlqBinding() {

        Binding binding = config.dlqBinding();

        assertNotNull(binding);
        assertEquals(
                RabbitMQConfig.DLQ,
                binding.getDestination());
        assertEquals(
                RabbitMQConfig.EXCHANGE,
                binding.getExchange());
        assertEquals(
                RabbitMQConfig.DLQ,
                binding.getRoutingKey());
    }

    @Test
    void shouldCreateJacksonMessageConverter() {

        MessageConverter converter =
                config.jsonMessageConverter();

        assertNotNull(converter);
        assertTrue(converter instanceof Jackson2JsonMessageConverter);
    }

    @Test
    void shouldCreateRabbitTemplate() {

        ConnectionFactory connectionFactory =
                mock(ConnectionFactory.class);

        RabbitTemplate template =
                config.rabbitTemplate(connectionFactory);

        assertNotNull(template);
        assertEquals(
                connectionFactory,
                template.getConnectionFactory());

        assertTrue(
                template.getMessageConverter()
                        instanceof Jackson2JsonMessageConverter);
    }
}
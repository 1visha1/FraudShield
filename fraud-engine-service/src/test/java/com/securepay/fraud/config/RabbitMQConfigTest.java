package com.fraudshield.fraud.config;

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
    }

    @Test
    void shouldCreateRuleEvaluatedQueue() {

        Queue queue = config.ruleEvaluatedQueue();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.RULE_EVALUATED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());

        assertEquals("", queue.getArguments().get("x-dead-letter-exchange"));
        assertEquals(
                RabbitMQConfig.RULE_EVALUATED_RETRY_QUEUE,
                queue.getArguments().get("x-dead-letter-routing-key"));
    }

    @Test
    void shouldCreateRuleEvaluatedDlq() {

        Queue queue = config.ruleEvaluatedDlq();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.RULE_EVALUATED_DLQ, queue.getName());
    }

    @Test
    void shouldCreateRetryQueue() {

        Queue queue = config.ruleEvaluatedRetryQueue();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.RULE_EVALUATED_RETRY_QUEUE, queue.getName());

        assertEquals("", queue.getArguments().get("x-dead-letter-exchange"));
        assertEquals(
                RabbitMQConfig.RULE_EVALUATED_QUEUE,
                queue.getArguments().get("x-dead-letter-routing-key"));

        assertEquals(5000, queue.getArguments().get("x-message-ttl"));
    }

    @Test
    void shouldCreateRuleBinding() {

        Binding binding = config.ruleEvaluatedBinding();

        assertNotNull(binding);
        assertEquals(RabbitMQConfig.RULE_EVALUATED_QUEUE, binding.getDestination());
        assertEquals(RabbitMQConfig.RULE_EVALUATED_KEY, binding.getRoutingKey());
        assertEquals(RabbitMQConfig.EXCHANGE, binding.getExchange());
    }

    @Test
    void shouldCreateDlqBinding() {

        Binding binding = config.ruleEvaluatedDlqBinding();

        assertNotNull(binding);
        assertEquals(RabbitMQConfig.RULE_EVALUATED_DLQ, binding.getDestination());
        assertEquals(RabbitMQConfig.RULE_EVALUATED_DLQ, binding.getRoutingKey());
        assertEquals(RabbitMQConfig.EXCHANGE, binding.getExchange());
    }

    @Test
    void shouldCreateRiskQueue() {

        Queue queue = config.riskAssessedQueue();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.RISK_ASSESSED_QUEUE, queue.getName());
    }

    @Test
    void shouldCreateRiskBinding() {

        Binding binding = config.riskAssessedBinding();

        assertNotNull(binding);
        assertEquals(RabbitMQConfig.RISK_ASSESSED_QUEUE, binding.getDestination());
        assertEquals(RabbitMQConfig.RISK_ASSESSED_KEY, binding.getRoutingKey());
        assertEquals(RabbitMQConfig.EXCHANGE, binding.getExchange());
    }

    @Test
    void shouldCreateJsonMessageConverter() {

        MessageConverter converter = config.jsonMessageConverter();

        assertNotNull(converter);
        assertTrue(converter instanceof Jackson2JsonMessageConverter);
    }

    @Test
    void shouldCreateRabbitTemplate() {

        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);

        RabbitTemplate rabbitTemplate =
                config.rabbitTemplate(connectionFactory);

        assertNotNull(rabbitTemplate);
        assertEquals(connectionFactory, rabbitTemplate.getConnectionFactory());
        assertTrue(
                rabbitTemplate.getMessageConverter()
                        instanceof Jackson2JsonMessageConverter);
    }
}
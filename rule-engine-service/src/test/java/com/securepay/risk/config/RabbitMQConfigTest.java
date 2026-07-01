package com.securepay.risk.config;

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
    void exchange_ShouldCreateTopicExchange() {

        TopicExchange exchange = config.exchange();

        assertNotNull(exchange);
        assertEquals(RabbitMQConfig.EXCHANGE, exchange.getName());
    }

    @Test
    void riskAssessedQueue_ShouldBeConfiguredCorrectly() {

        Queue queue = config.riskAssessedQueue();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.RISK_ASSESSED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());

        assertEquals(
                "",
                queue.getArguments().get("x-dead-letter-exchange")
        );

        assertEquals(
                RabbitMQConfig.RISK_ASSESSED_RETRY_QUEUE,
                queue.getArguments().get("x-dead-letter-routing-key")
        );
    }

    @Test
    void riskAssessedDlq_ShouldBeConfiguredCorrectly() {

        Queue queue = config.riskAssessedDlq();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.RISK_ASSESSED_DLQ, queue.getName());
    }

    @Test
    void riskAssessedRetryQueue_ShouldBeConfiguredCorrectly() {

        Queue queue = config.riskAssessedRetryQueue();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.RISK_ASSESSED_RETRY_QUEUE, queue.getName());
        assertTrue(queue.isDurable());

        assertEquals(
                "",
                queue.getArguments().get("x-dead-letter-exchange")
        );

        assertEquals(
                RabbitMQConfig.RISK_ASSESSED_QUEUE,
                queue.getArguments().get("x-dead-letter-routing-key")
        );

        assertEquals(
                5000,
                queue.getArguments().get("x-message-ttl")
        );
    }

    @Test
    void riskAssessedBinding_ShouldBeCreated() {

        Binding binding = config.riskAssessedBinding();

        assertNotNull(binding);
        assertEquals(
                RabbitMQConfig.RISK_ASSESSED_QUEUE,
                binding.getDestination()
        );
    }

    @Test
    void riskAssessedDlqBinding_ShouldBeCreated() {

        Binding binding = config.riskAssessedDlqBinding();

        assertNotNull(binding);
        assertEquals(
                RabbitMQConfig.RISK_ASSESSED_DLQ,
                binding.getDestination()
        );
    }

    @Test
    void jsonMessageConverter_ShouldReturnJacksonConverter() {

        MessageConverter converter = config.jsonMessageConverter();

        assertNotNull(converter);
        assertInstanceOf(
                Jackson2JsonMessageConverter.class,
                converter
        );
    }

    @Test
    void rabbitTemplate_ShouldCreateRabbitTemplate() {

        ConnectionFactory connectionFactory =
                mock(ConnectionFactory.class);

        RabbitTemplate template =
                config.rabbitTemplate(connectionFactory);

        assertNotNull(template);
        assertInstanceOf(
                Jackson2JsonMessageConverter.class,
                template.getMessageConverter()
        );
    }
}
package com.fraudshield.notification.config;

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
    void authChallengeQueue_ShouldCreateQueue() {

        Queue queue = config.authChallengeQueue();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.AUTH_CHALLENGE_QUEUE, queue.getName());
        assertTrue(queue.isDurable());

        assertEquals(
                "",
                queue.getArguments().get("x-dead-letter-exchange")
        );

        assertEquals(
                RabbitMQConfig.AUTH_CHALLENGE_RETRY_QUEUE,
                queue.getArguments().get("x-dead-letter-routing-key")
        );
    }

    @Test
    void authChallengeDlq_ShouldCreateQueue() {

        Queue queue = config.authChallengeDlq();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.AUTH_CHALLENGE_DLQ, queue.getName());
    }

    @Test
    void authChallengeRetryQueue_ShouldCreateQueue() {

        Queue queue = config.authChallengeRetryQueue();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.AUTH_CHALLENGE_RETRY_QUEUE, queue.getName());

        assertEquals(
                "",
                queue.getArguments().get("x-dead-letter-exchange")
        );

        assertEquals(
                RabbitMQConfig.AUTH_CHALLENGE_QUEUE,
                queue.getArguments().get("x-dead-letter-routing-key")
        );

        assertEquals(
                5000,
                queue.getArguments().get("x-message-ttl")
        );
    }

    @Test
    void authChallengeBinding_ShouldBindQueueToExchange() {

        Binding binding = config.authChallengeBinding();

        assertNotNull(binding);

        assertEquals(
                RabbitMQConfig.AUTH_CHALLENGE_QUEUE,
                binding.getDestination()
        );

        assertEquals(
                RabbitMQConfig.EXCHANGE,
                binding.getExchange()
        );

        assertEquals(
                RabbitMQConfig.AUTH_CHALLENGE_KEY,
                binding.getRoutingKey()
        );
    }

    @Test
    void authChallengeDlqBinding_ShouldBindDlqToExchange() {

        Binding binding = config.authChallengeDlqBinding();

        assertNotNull(binding);

        assertEquals(
                RabbitMQConfig.AUTH_CHALLENGE_DLQ,
                binding.getDestination()
        );

        assertEquals(
                RabbitMQConfig.EXCHANGE,
                binding.getExchange()
        );

        assertEquals(
                RabbitMQConfig.AUTH_CHALLENGE_DLQ,
                binding.getRoutingKey()
        );
    }

    @Test
    void jsonMessageConverter_ShouldCreateJacksonConverter() {

        MessageConverter converter = config.jsonMessageConverter();

        assertNotNull(converter);
        assertInstanceOf(Jackson2JsonMessageConverter.class, converter);
    }

    @Test
    void rabbitTemplate_ShouldCreateRabbitTemplate() {

        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);

        RabbitTemplate rabbitTemplate =
                config.rabbitTemplate(connectionFactory);

        assertNotNull(rabbitTemplate);
        assertSame(connectionFactory, rabbitTemplate.getConnectionFactory());

        assertNotNull(rabbitTemplate.getMessageConverter());
        assertTrue(
                rabbitTemplate.getMessageConverter()
                        instanceof Jackson2JsonMessageConverter
        );
    }
}
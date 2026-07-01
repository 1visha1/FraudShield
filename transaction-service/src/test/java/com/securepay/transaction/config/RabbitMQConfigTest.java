package com.securepay.transaction.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securepay.transaction.event.AuthChallengeCompletedEvent;
import com.securepay.transaction.event.TransactionBlockedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
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
    void challengeCompletedQueue_ShouldBeConfiguredCorrectly() {

        Queue queue = config.challengeCompletedQueue();

        assertEquals(RabbitMQConfig.CHALLENGE_COMPLETED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void challengeCompletedDlq_ShouldBeConfiguredCorrectly() {

        Queue queue = config.challengeCompletedDlq();

        assertEquals(RabbitMQConfig.CHALLENGE_COMPLETED_DLQ, queue.getName());
    }

    @Test
    void challengeCompletedRetryQueue_ShouldBeConfiguredCorrectly() {

        Queue queue = config.challengeCompletedRetryQueue();

        assertEquals(RabbitMQConfig.CHALLENGE_COMPLETED_RETRY_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void blockedQueue_ShouldBeConfiguredCorrectly() {

        Queue queue = config.blockedQueue();

        assertEquals(RabbitMQConfig.BLOCKED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void blockedDlq_ShouldBeConfiguredCorrectly() {

        Queue queue = config.blockedDlq();

        assertEquals(RabbitMQConfig.BLOCKED_DLQ, queue.getName());
    }

    @Test
    void blockedRetryQueue_ShouldBeConfiguredCorrectly() {

        Queue queue = config.blockedRetryQueue();

        assertEquals(RabbitMQConfig.BLOCKED_RETRY_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
    }

    @Test
    void challengeCompletedBinding_ShouldBeCreated() {

        Binding binding = config.challengeCompletedBinding();

        assertNotNull(binding);
        assertEquals(RabbitMQConfig.CHALLENGE_COMPLETED_QUEUE,
                binding.getDestination());
    }

    @Test
    void challengeCompletedDlqBinding_ShouldBeCreated() {

        Binding binding = config.challengeCompletedDlqBinding();

        assertNotNull(binding);
        assertEquals(RabbitMQConfig.CHALLENGE_COMPLETED_DLQ,
                binding.getDestination());
    }

    @Test
    void blockedBinding_ShouldBeCreated() {

        Binding binding = config.blockedBinding();

        assertNotNull(binding);
        assertEquals(RabbitMQConfig.BLOCKED_QUEUE,
                binding.getDestination());
    }

    @Test
    void blockedDlqBinding_ShouldBeCreated() {

        Binding binding = config.blockedDlqBinding();

        assertNotNull(binding);
        assertEquals(RabbitMQConfig.BLOCKED_DLQ,
                binding.getDestination());
    }

    @Test
    void classMapper_ShouldContainMappings() {

        DefaultClassMapper mapper = config.classMapper();

        assertNotNull(mapper);

        assertEquals(
                AuthChallengeCompletedEvent.class,
                mapper.toClass(
                        new org.springframework.amqp.core.MessageProperties() {{
                            setHeader("__TypeId__",
                                    "com.securepay.auth.event.AuthChallengeCompletedEvent");
                        }}
                )
        );

        assertEquals(
                TransactionBlockedEvent.class,
                mapper.toClass(
                        new org.springframework.amqp.core.MessageProperties() {{
                            setHeader("__TypeId__",
                                    "com.securepay.auth.event.TransactionBlockedEvent");
                        }}
                )
        );
    }

    @Test
    void jsonMessageConverter_ShouldReturnJacksonConverter() {

        ObjectMapper objectMapper = new ObjectMapper();

        MessageConverter converter =
                config.jsonMessageConverter(objectMapper);

        assertNotNull(converter);
        assertTrue(converter instanceof Jackson2JsonMessageConverter);
    }

    @Test
    void rabbitListenerContainerFactory_ShouldCreateFactory() {

        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        MessageConverter converter = mock(MessageConverter.class);

        SimpleRabbitListenerContainerFactory factory =
                config.rabbitListenerContainerFactory(
                        connectionFactory,
                        converter
                );

        assertNotNull(factory);
    }

    @Test
    void rabbitTemplate_ShouldCreateRabbitTemplate() {

        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        MessageConverter converter = mock(MessageConverter.class);

        RabbitTemplate template =
                config.rabbitTemplate(connectionFactory, converter);

        assertNotNull(template);
        assertEquals(converter, template.getMessageConverter());
    }
}
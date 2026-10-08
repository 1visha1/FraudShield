package com.fraudshield.auth.config;

import com.fraudshield.auth.event.AuthChallengeCompletedEvent;
import com.fraudshield.auth.event.FraudDetectedEvent;
import com.fraudshield.auth.event.OtpVerifiedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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
    void exchange_ShouldCreateExchange() {
        TopicExchange exchange = config.exchange();

        assertNotNull(exchange);
        assertEquals(RabbitMQConfig.EXCHANGE, exchange.getName());
    }

    @Test
    void fraudQueue_ShouldCreateQueue() {
        Queue queue = config.fraudQueue();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.FRAUD_QUEUE, queue.getName());
    }

    @Test
    void otpVerifiedQueue_ShouldCreateQueue() {
        Queue queue = config.otpVerifiedQueue();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.OTP_VERIFIED_QUEUE, queue.getName());
    }

    @Test
    void challengeCompletedNotificationQueue_ShouldCreateQueue() {
        Queue queue = config.challengeCompletedNotificationQueue();

        assertNotNull(queue);
        assertEquals(RabbitMQConfig.CHALLENGE_COMPLETED_NOTIFICATION_QUEUE, queue.getName());
    }

    @Test
    void fraudBinding_ShouldCreateBinding() {

        Binding binding = config.fraudBinding(
                config.fraudQueue(),
                config.exchange());

        assertNotNull(binding);
        assertEquals(RabbitMQConfig.FRAUD_KEY, binding.getRoutingKey());
    }

    @Test
    void otpVerifiedBinding_ShouldCreateBinding() {

        Binding binding = config.otpVerifiedBinding(
                config.otpVerifiedQueue(),
                config.exchange());

        assertNotNull(binding);
        assertEquals(RabbitMQConfig.OTP_VERIFIED_KEY, binding.getRoutingKey());
    }

    @Test
    void challengeCompletedNotificationBinding_ShouldCreateBinding() {

        Binding binding = config.challengeCompletedNotificationBinding(
                config.challengeCompletedNotificationQueue(),
                config.exchange());

        assertNotNull(binding);
        assertEquals(
                RabbitMQConfig.CHALLENGE_COMPLETED_NOTIFICATION_KEY,
                binding.getRoutingKey());
    }

    @Test
    void classMapper_ShouldCreateClassMapper() {

        DefaultClassMapper mapper = config.classMapper();

        assertNotNull(mapper);
    }

    @Test
    void jsonMessageConverter_ShouldCreateConverter() {

        MessageConverter converter = config.jsonMessageConverter();

        assertNotNull(converter);
        assertTrue(converter instanceof Jackson2JsonMessageConverter);
    }

    @Test
    void rabbitListenerContainerFactory_ShouldCreateFactory() {

        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);

        SimpleRabbitListenerContainerFactory factory =
                config.rabbitListenerContainerFactory(connectionFactory);

        assertNotNull(factory);
    }

    @Test
    void rabbitTemplate_ShouldCreateRabbitTemplate() {

        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);

        RabbitTemplate template =
                config.rabbitTemplate(connectionFactory);

        assertNotNull(template);
        assertSame(connectionFactory, template.getConnectionFactory());
        assertTrue(template.getMessageConverter()
                instanceof Jackson2JsonMessageConverter);
    }

    @Test
    void constants_ShouldHaveExpectedValues() {

        assertEquals("fraudshield.exchange", RabbitMQConfig.EXCHANGE);
        assertEquals("auth.fraud.detected.q", RabbitMQConfig.FRAUD_QUEUE);
        assertEquals("fraud.detected", RabbitMQConfig.FRAUD_KEY);

        assertEquals("auth.otp.verified.q",
                RabbitMQConfig.OTP_VERIFIED_QUEUE);
        assertEquals("otp.verified",
                RabbitMQConfig.OTP_VERIFIED_KEY);

        assertEquals(
                "auth.challenge.completed.notification.q",
                RabbitMQConfig.CHALLENGE_COMPLETED_NOTIFICATION_QUEUE);

        assertEquals(
                "auth.challenge.completed.notification",
                RabbitMQConfig.CHALLENGE_COMPLETED_NOTIFICATION_KEY);
    }
}
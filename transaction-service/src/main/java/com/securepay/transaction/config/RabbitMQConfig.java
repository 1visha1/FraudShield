package com.fraudshield.transaction.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudshield.transaction.event.AuthChallengeCompletedEvent;
import com.fraudshield.transaction.event.TransactionBlockedEvent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "fraudshield.exchange";
    public static final String ROUTING_KEY = "transaction.created";

    public static final String CHALLENGE_COMPLETED_QUEUE = "transaction.challenge.completed.q";
    public static final String CHALLENGE_COMPLETED_KEY = "auth.challenge.completed";
    public static final String CHALLENGE_COMPLETED_DLQ = "transaction.challenge.completed.dlq";
    public static final String CHALLENGE_COMPLETED_RETRY_QUEUE = "transaction.challenge.completed.retry.q";

    public static final String BLOCKED_QUEUE = "transaction.blocked.q";
    public static final String BLOCKED_KEY = "transaction.blocked";
    public static final String BLOCKED_DLQ = "transaction.blocked.dlq";
    public static final String BLOCKED_RETRY_QUEUE = "transaction.blocked.retry.q";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Challenge Completed Queue, DLQ, and Retry Queue
    @Bean
    public Queue challengeCompletedQueue() {
        return QueueBuilder.durable(CHALLENGE_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", CHALLENGE_COMPLETED_RETRY_QUEUE)
                .build();
    }

    @Bean
    public Queue challengeCompletedDlq() {
        return new Queue(CHALLENGE_COMPLETED_DLQ);
    }

    @Bean
    public Queue challengeCompletedRetryQueue() {
        return QueueBuilder.durable(CHALLENGE_COMPLETED_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", CHALLENGE_COMPLETED_QUEUE)
                .withArgument("x-message-ttl", 5000)
                .build();
    }

    @Bean
    public Binding challengeCompletedBinding() {
        return BindingBuilder.bind(challengeCompletedQueue()).to(exchange()).with(CHALLENGE_COMPLETED_KEY);
    }

    @Bean
    public Binding challengeCompletedDlqBinding() {
        return BindingBuilder.bind(challengeCompletedDlq()).to(exchange()).with(CHALLENGE_COMPLETED_DLQ);
    }

    // Blocked Queue, DLQ, and Retry Queue
    @Bean
    public Queue blockedQueue() {
        return QueueBuilder.durable(BLOCKED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", BLOCKED_RETRY_QUEUE)
                .build();
    }

    @Bean
    public Queue blockedDlq() {
        return new Queue(BLOCKED_DLQ);
    }

    @Bean
    public Queue blockedRetryQueue() {
        return QueueBuilder.durable(BLOCKED_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", BLOCKED_QUEUE)
                .withArgument("x-message-ttl", 5000)
                .build();
    }

    @Bean
    public Binding blockedBinding() {
        return BindingBuilder.bind(blockedQueue()).to(exchange()).with(BLOCKED_KEY);
    }

    @Bean
    public Binding blockedDlqBinding() {
        return BindingBuilder.bind(blockedDlq()).to(exchange()).with(BLOCKED_DLQ);
    }


    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setClassMapper(classMapper());
        return converter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("com.fraudshield.auth.event.AuthChallengeCompletedEvent", AuthChallengeCompletedEvent.class);
        idClassMapping.put("com.fraudshield.auth.event.TransactionBlockedEvent", TransactionBlockedEvent.class);
        classMapper.setIdClassMapping(idClassMapping);
        classMapper.setTrustedPackages("*");
        return classMapper;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
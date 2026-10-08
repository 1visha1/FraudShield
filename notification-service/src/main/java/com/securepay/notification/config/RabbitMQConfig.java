package com.fraudshield.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "fraudshield.exchange";

    public static final String AUTH_CHALLENGE_QUEUE = "notification.auth.challenge.q";
    public static final String AUTH_CHALLENGE_KEY = "auth.challenge.created";
    public static final String AUTH_CHALLENGE_DLQ = "notification.auth.challenge.dlq";
    public static final String AUTH_CHALLENGE_RETRY_QUEUE = "notification.auth.challenge.retry.q";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Auth Challenge Queue, DLQ, and Retry Queue
    @Bean
    public Queue authChallengeQueue() {
        return QueueBuilder.durable(AUTH_CHALLENGE_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", AUTH_CHALLENGE_RETRY_QUEUE)
                .build();
    }

    @Bean
    public Queue authChallengeDlq() {
        return new Queue(AUTH_CHALLENGE_DLQ);
    }

    @Bean
    public Queue authChallengeRetryQueue() {
        return QueueBuilder.durable(AUTH_CHALLENGE_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", AUTH_CHALLENGE_QUEUE)
                .withArgument("x-message-ttl", 5000)
                .build();
    }

    @Bean
    public Binding authChallengeBinding() {
        return BindingBuilder.bind(authChallengeQueue()).to(exchange()).with(AUTH_CHALLENGE_KEY);
    }

    @Bean
    public Binding authChallengeDlqBinding() {
        return BindingBuilder.bind(authChallengeDlq()).to(exchange()).with(AUTH_CHALLENGE_DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper typeMapper = new org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");
        java.util.Map<String, Class<?>> idClassMapping = new java.util.HashMap<>();
        idClassMapping.put("com.fraudshield.auth.event.AuthChallengeCreatedEvent", com.fraudshield.notification.event.AuthChallengeCreatedEvent.class);
        typeMapper.setIdClassMapping(idClassMapping);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
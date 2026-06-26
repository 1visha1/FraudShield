package com.securepay.transaction.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securepay.transaction.event.AuthChallengeCompletedEvent;
import com.securepay.transaction.event.TransactionBlockedEvent;
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

    public static final String EXCHANGE = "securepay.exchange";
    public static final String ROUTING_KEY = "transaction.created";
    
    public static final String CHALLENGE_COMPLETED_QUEUE = "transaction.challenge.completed.q";
    public static final String CHALLENGE_COMPLETED_KEY = "auth.challenge.completed";

    public static final String BLOCKED_QUEUE = "transaction.blocked.q";
    public static final String BLOCKED_KEY = "transaction.blocked";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue challengeCompletedQueue() {
        return new Queue(CHALLENGE_COMPLETED_QUEUE);
    }

    @Bean
    public Queue blockedQueue() {
        return new Queue(BLOCKED_QUEUE);
    }

    @Bean
    public Binding challengeCompletedBinding(Queue challengeCompletedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(challengeCompletedQueue).to(exchange).with(CHALLENGE_COMPLETED_KEY);
    }

    @Bean
    public Binding blockedBinding(Queue blockedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(blockedQueue).to(exchange).with(BLOCKED_KEY);
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
        idClassMapping.put("com.securepay.auth.event.AuthChallengeCompletedEvent", AuthChallengeCompletedEvent.class);
        idClassMapping.put("com.securepay.auth.event.TransactionBlockedEvent", TransactionBlockedEvent.class);
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
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
package com.securepay.audit.config;

import com.securepay.audit.event.*;
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
    public static final String CHALLENGE_COMPLETED_QUEUE = "audit.auth.challenge.completed.q";
    public static final String TRANSACTION_APPROVED_QUEUE = "audit.transaction.approved.q";
    public static final String TRANSACTION_BLOCKED_QUEUE = "audit.transaction.blocked.q";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue challengeCompletedQueue() {
        return new Queue(CHALLENGE_COMPLETED_QUEUE);
    }

    @Bean
    public Queue transactionApprovedQueue() {
        return new Queue(TRANSACTION_APPROVED_QUEUE);
    }

    @Bean
    public Queue transactionBlockedQueue() {
        return new Queue(TRANSACTION_BLOCKED_QUEUE);
    }

    @Bean
    public Binding challengeCompletedBinding(Queue challengeCompletedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(challengeCompletedQueue).to(exchange).with("auth.challenge.completed");
    }

    @Bean
    public Binding transactionApprovedBinding(Queue transactionApprovedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(transactionApprovedQueue).to(exchange).with("transaction.approved");
    }

    @Bean
    public Binding transactionBlockedBinding(Queue transactionBlockedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(transactionBlockedQueue).to(exchange).with("transaction.blocked");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper());
        return converter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        
        idClassMapping.put("com.securepay.device.event.DeviceVerifiedEvent",   DeviceVerifiedEvent.class);
        idClassMapping.put("com.securepay.risk.event.RiskAssessedEvent",       RiskAssessedEvent.class);
        idClassMapping.put("com.securepay.fraud.event.FraudDetectedEvent",      FraudDetectedEvent.class);
        idClassMapping.put("com.securepay.auth.event.AuthChallengeSentEvent",  AuthChallengeSentEvent.class);
        idClassMapping.put("com.securepay.auth.event.AuthChallengeCompletedEvent", AuthChallengeCompletedEvent.class);
        idClassMapping.put("com.securepay.transaction.event.TransactionApprovedEvent", TransactionApprovedEvent.class);
        idClassMapping.put("com.securepay.auth.event.TransactionBlockedEvent", TransactionBlockedEvent.class);
        
        classMapper.setIdClassMapping(idClassMapping);
        classMapper.setTrustedPackages("*");
        return classMapper;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
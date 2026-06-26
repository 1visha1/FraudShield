package com.securepay.risk.config;

import com.securepay.risk.event.DeviceVerifiedEvent;
import com.securepay.risk.event.RiskAssessedEvent;
import com.securepay.risk.event.TransactionCreatedEvent;
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
    public static final String TRANSACTION_QUEUE = "transaction.created.q";
    public static final String DEVICE_QUEUE = "device.verified.q";
    public static final String RISK_QUEUE = "risk.assessed.q";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue transactionQueue() {
        return QueueBuilder.durable(TRANSACTION_QUEUE).build();
    }

    @Bean
    public Queue deviceQueue() {
        return QueueBuilder.durable(DEVICE_QUEUE).build();
    }

    @Bean
    public Queue riskQueue() {
        return QueueBuilder.durable(RISK_QUEUE).build();
    }

    @Bean
    public Binding transactionBinding(Queue transactionQueue, TopicExchange exchange) {
        return BindingBuilder.bind(transactionQueue).to(exchange).with("transaction.created");
    }

    @Bean
    public Binding deviceBinding(Queue deviceQueue, TopicExchange exchange) {
        return BindingBuilder.bind(deviceQueue).to(exchange).with("device.verified");
    }

    @Bean
    public Binding riskBinding(Queue riskQueue, TopicExchange exchange) {
        return BindingBuilder.bind(riskQueue).to(exchange).with("risk.assessed");
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
        
        // Incoming mappings
        idClassMapping.put("com.securepay.transaction.event.TransactionCreatedEvent", TransactionCreatedEvent.class);
        idClassMapping.put("com.securepay.device.event.DeviceVerifiedEvent", DeviceVerifiedEvent.class);
        
        // Outgoing mapping (so Fraud service recognizes it)
        idClassMapping.put("com.securepay.risk.event.RiskAssessedEvent", RiskAssessedEvent.class);
        
        classMapper.setIdClassMapping(idClassMapping);
        classMapper.setTrustedPackages("*");
        return classMapper;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
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
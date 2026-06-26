package com.securepay.fraud.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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

    public static final String EXCHANGE_NAME = "securepay.exchange";
    // UNIQUE QUEUE NAMES for this service
    public static final String RISK_QUEUE = "fraud.risk.assessed.q";
    public static final String RULE_QUEUE = "fraud.rule.evaluated.q";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue riskQueue() {
        return new Queue(RISK_QUEUE);
    }

    @Bean
    public Queue ruleQueue() {
        return new Queue(RULE_QUEUE);
    }

    @Bean
    public Binding riskBinding(Queue riskQueue, TopicExchange exchange) {
        return BindingBuilder.bind(riskQueue).to(exchange).with("risk.assessed");
    }

    @Bean
    public Binding ruleBinding(Queue ruleQueue, TopicExchange exchange) {
        return BindingBuilder.bind(ruleQueue).to(exchange).with("rule.evaluated");
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
        idClassMapping.put("com.securepay.risk.event.RiskAssessedEvent", 
                com.securepay.fraud.event.RiskAssessedEvent.class);
        idClassMapping.put("com.securepay.rule.event.RuleEvaluatedEvent", 
                com.securepay.fraud.event.RuleEvaluatedEvent.class);
        classMapper.setIdClassMapping(idClassMapping);
        classMapper.setTrustedPackages("*");
        return classMapper;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
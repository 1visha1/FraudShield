package com.fraudshield.fraud.config;

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

    public static final String RULE_EVALUATED_QUEUE = "fraud.rule.evaluated.q";
    public static final String RULE_EVALUATED_KEY = "rule.evaluated";
    public static final String RULE_EVALUATED_DLQ = "fraud.rule.evaluated.dlq";
    public static final String RULE_EVALUATED_RETRY_QUEUE = "fraud.rule.evaluated.retry.q";

    public static final String FRAUD_DETECTED_KEY = "fraud.detected";

    public static final String RISK_ASSESSED_QUEUE = "fraud.risk.assessed.q";
    public static final String RISK_ASSESSED_KEY = "risk.assessed";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Rule Evaluated Queue, DLQ, and Retry Queue
    @Bean
    public Queue ruleEvaluatedQueue() {
        return QueueBuilder.durable(RULE_EVALUATED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", RULE_EVALUATED_RETRY_QUEUE)
                .build();
    }

    @Bean
    public Queue ruleEvaluatedDlq() {
        return new Queue(RULE_EVALUATED_DLQ);
    }

    @Bean
    public Queue ruleEvaluatedRetryQueue() {
        return QueueBuilder.durable(RULE_EVALUATED_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", RULE_EVALUATED_QUEUE)
                .withArgument("x-message-ttl", 5000)
                .build();
    }

    @Bean
    public Binding ruleEvaluatedBinding() {
        return BindingBuilder.bind(ruleEvaluatedQueue()).to(exchange()).with(RULE_EVALUATED_KEY);
    }

    @Bean
    public Binding ruleEvaluatedDlqBinding() {
        return BindingBuilder.bind(ruleEvaluatedDlq()).to(exchange()).with(RULE_EVALUATED_DLQ);
    }

    @Bean
    public Queue riskAssessedQueue() {
        return new Queue(RISK_ASSESSED_QUEUE);
    }

    @Bean
    public Binding riskAssessedBinding() {
        return BindingBuilder.bind(riskAssessedQueue()).to(exchange()).with(RISK_ASSESSED_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
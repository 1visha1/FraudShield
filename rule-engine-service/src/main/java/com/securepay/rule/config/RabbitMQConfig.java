package com.securepay.rule.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "securepay.exchange";

    public static final String RISK_ASSESSED_QUEUE = "rule.risk.assessed.q";
    public static final String RISK_ASSESSED_KEY = "risk.assessed";
    public static final String RISK_ASSESSED_DLQ = "rule.risk.assessed.dlq";
    public static final String RISK_ASSESSED_RETRY_QUEUE = "rule.risk.assessed.retry.q";

    public static final String RULE_EVALUATED_KEY = "rule.evaluated";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Risk Assessed Queue, DLQ, and Retry Queue
    @Bean
    public Queue riskAssessedQueue() {
        return QueueBuilder.durable(RISK_ASSESSED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", RISK_ASSESSED_RETRY_QUEUE)
                .build();
    }

    @Bean
    public Queue riskAssessedDlq() {
        return new Queue(RISK_ASSESSED_DLQ);
    }

    @Bean
    public Queue riskAssessedRetryQueue() {
        return QueueBuilder.durable(RISK_ASSESSED_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", RISK_ASSESSED_QUEUE)
                .withArgument("x-message-ttl", 5000)
                .build();
    }

    @Bean
    public Binding riskAssessedBinding() {
        return BindingBuilder.bind(riskAssessedQueue()).to(exchange()).with(RISK_ASSESSED_KEY);
    }

    @Bean
    public Binding riskAssessedDlqBinding() {
        return BindingBuilder.bind(riskAssessedDlq()).to(exchange()).with(RISK_ASSESSED_DLQ);
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
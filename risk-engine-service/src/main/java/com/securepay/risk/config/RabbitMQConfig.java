package com.securepay.risk.config;

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

    public static final String TRANSACTION_CREATED_QUEUE = "risk.transaction.created.q";
    public static final String TRANSACTION_CREATED_KEY = "transaction.created";
    public static final String TRANSACTION_CREATED_DLQ = "risk.transaction.created.dlq";
    public static final String TRANSACTION_CREATED_RETRY_QUEUE = "risk.transaction.created.retry.q";

    public static final String RISK_ASSESSED_KEY = "risk.assessed";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Transaction Created Queue, DLQ, and Retry Queue
    @Bean
    public Queue transactionCreatedQueue() {
        return QueueBuilder.durable(TRANSACTION_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", TRANSACTION_CREATED_RETRY_QUEUE)
                .build();
    }

    @Bean
    public Queue transactionCreatedDlq() {
        return new Queue(TRANSACTION_CREATED_DLQ);
    }

    @Bean
    public Queue transactionCreatedRetryQueue() {
        return QueueBuilder.durable(TRANSACTION_CREATED_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", TRANSACTION_CREATED_QUEUE)
                .withArgument("x-message-ttl", 5000)
                .build();
    }

    @Bean
    public Binding transactionCreatedBinding() {
        return BindingBuilder.bind(transactionCreatedQueue()).to(exchange()).with(TRANSACTION_CREATED_KEY);
    }

    @Bean
    public Binding transactionCreatedDlqBinding() {
        return BindingBuilder.bind(transactionCreatedDlq()).to(exchange()).with(TRANSACTION_CREATED_DLQ);
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
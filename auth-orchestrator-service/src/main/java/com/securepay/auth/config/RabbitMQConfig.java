package com.securepay.auth.config;

import com.securepay.auth.event.FraudDetectedEvent;
import com.securepay.auth.event.OtpVerifiedEvent;
import com.securepay.auth.event.AuthChallengeCompletedEvent;
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

    public static final String EXCHANGE    = "securepay.exchange";
    public static final String FRAUD_QUEUE = "auth.fraud.detected.q";
    public static final String FRAUD_KEY   = "fraud.detected";
    
    public static final String OTP_VERIFIED_QUEUE = "auth.otp.verified.q";
    public static final String OTP_VERIFIED_KEY = "otp.verified";

    public static final String CHALLENGE_COMPLETED_NOTIFICATION_QUEUE = "auth.challenge.completed.notification.q";
    public static final String CHALLENGE_COMPLETED_NOTIFICATION_KEY = "auth.challenge.completed.notification";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue fraudQueue() {
        return new Queue(FRAUD_QUEUE);
    }

    @Bean
    public Queue otpVerifiedQueue() {
        return new Queue(OTP_VERIFIED_QUEUE);
    }

    @Bean
    public Queue challengeCompletedNotificationQueue() {
        return new Queue(CHALLENGE_COMPLETED_NOTIFICATION_QUEUE);
    }

    @Bean
    public Binding fraudBinding(Queue fraudQueue, TopicExchange exchange) {
        return BindingBuilder.bind(fraudQueue).to(exchange).with(FRAUD_KEY);
    }

    @Bean
    public Binding otpVerifiedBinding(Queue otpVerifiedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(otpVerifiedQueue).to(exchange).with(OTP_VERIFIED_KEY);
    }

    @Bean
    public Binding challengeCompletedNotificationBinding(Queue challengeCompletedNotificationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(challengeCompletedNotificationQueue).to(exchange).with(CHALLENGE_COMPLETED_NOTIFICATION_KEY);
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
        idClassMapping.put("com.securepay.fraud.event.FraudDetectedEvent", FraudDetectedEvent.class);
        idClassMapping.put("com.securepay.notification.event.OtpVerifiedEvent", OtpVerifiedEvent.class);
        idClassMapping.put("com.securepay.notification.event.AuthChallengeCompletedEvent", AuthChallengeCompletedEvent.class);
        
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
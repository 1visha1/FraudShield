package com.securepay.notification.consumer;


import com.securepay.notification.event.AuthChallengeCreatedEvent;
import com.securepay.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthChallengeConsumer {

    private final NotificationService service;

    @RabbitListener(
            queues =
                    "auth.challenge.created.q")
    public void consume(
            AuthChallengeCreatedEvent event){

        service.process(event);
    }
}
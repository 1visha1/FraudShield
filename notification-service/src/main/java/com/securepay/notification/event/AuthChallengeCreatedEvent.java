package com.securepay.notification.event;

import lombok.Data;

import java.util.UUID;

@Data
public class AuthChallengeCreatedEvent {

    private UUID transactionId;

    private UUID customerId;

    private String authType;

    private String status;
}
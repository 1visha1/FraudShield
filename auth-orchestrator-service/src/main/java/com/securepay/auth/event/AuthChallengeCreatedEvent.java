package com.fraudshield.auth.event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthChallengeCreatedEvent {

    private UUID transactionId;

    private UUID customerId;

    private String authType;

    private String status;
}
package com.fraudshield.audit.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthChallengeCompletedEvent {
    private UUID transactionId;
    private UUID customerId;
    private UUID authSessionId;
    private boolean verified;
    private LocalDateTime verifiedAt;
}
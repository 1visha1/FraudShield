package com.securepay.transaction.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthChallengeCompletedEvent {
    private UUID transactionId;
    private UUID customerId;
    private UUID authSessionId;
    private boolean verified;
    private LocalDateTime verifiedAt;
}
package com.securepay.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthChallengeCompletedEvent {
    private UUID transactionId;
    private String status; // "COMPLETED" or "FAILED"
}

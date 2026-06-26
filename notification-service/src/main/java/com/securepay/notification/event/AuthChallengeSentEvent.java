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
public class AuthChallengeSentEvent {

    private UUID transactionId;

    private UUID customerId;

    private String authType;

    private String deliveryStatus;
}

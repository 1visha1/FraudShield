package com.securepay.device.event;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceVerifiedEvent {

    private String eventType;

    private UUID customerId;

    private UUID deviceId;

    private Boolean trusted;

    private Integer deviceRiskScore;
}
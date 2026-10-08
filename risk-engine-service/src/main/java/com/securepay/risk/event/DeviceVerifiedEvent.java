package com.fraudshield.risk.event;

import lombok.Data;

import java.util.UUID;

@Data
public class DeviceVerifiedEvent {

    private String eventType;

    private UUID customerId;

    private UUID deviceId;

    private Boolean trusted;

    private Integer deviceRiskScore;
}
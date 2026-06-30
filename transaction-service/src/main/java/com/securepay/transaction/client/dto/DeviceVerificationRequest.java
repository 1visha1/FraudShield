package com.securepay.transaction.client.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DeviceVerificationRequest {

    private UUID customerId;
    private UUID deviceId;
    private String userAgent;
    private String ipAddress;
    private String timezone;
    private String osVersion;
}

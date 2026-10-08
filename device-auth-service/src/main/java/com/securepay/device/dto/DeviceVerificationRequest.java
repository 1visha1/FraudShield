package com.fraudshield.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DeviceVerificationRequest {

    @NotNull
    private UUID customerId;

    @NotNull
    private UUID deviceId;

    @NotBlank
    private String userAgent;

    @NotBlank
    private String ipAddress;

    @NotBlank
    private String timezone;

    @NotBlank
    private String osVersion;
}
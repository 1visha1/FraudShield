package com.securepay.device.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceVerificationResponse {

    private Boolean trusted;

    private Integer deviceRiskScore;

    private String fingerprint;
}
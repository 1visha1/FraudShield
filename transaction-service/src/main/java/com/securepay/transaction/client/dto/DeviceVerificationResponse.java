package com.fraudshield.transaction.client.dto;

import lombok.Data;

@Data
public class DeviceVerificationResponse {

    private Boolean trusted;
    private Integer deviceRiskScore;
    private String fingerprint;
}

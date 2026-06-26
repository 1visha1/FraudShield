package com.securepay.audit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditDocument {

    private String eventType;

    private String serviceName;

    private String transactionId;

    private String payload;

    private String timestamp;
}
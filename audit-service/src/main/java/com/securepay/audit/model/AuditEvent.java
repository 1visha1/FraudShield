package com.securepay.audit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    private String eventType;

    private String serviceName;

    private String transactionId;

    private String payload;

    private LocalDateTime timestamp;
}
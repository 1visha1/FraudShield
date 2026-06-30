package com.securepay.audit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "audit-events", createIndex = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    @Id
    private String id;
    private String eventData;
    private String eventType;
    private String serviceName;
    private String transactionId;
    private String payload;
    private java.time.LocalDateTime timestamp;
}
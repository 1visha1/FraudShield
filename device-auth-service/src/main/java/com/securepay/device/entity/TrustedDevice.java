package com.securepay.device.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trusted_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustedDevice {

    @Id
    private UUID deviceId;

    private UUID customerId;

    private String fingerprint;

    private Integer riskScore;

    private Boolean trusted;

    private LocalDateTime createdAt;

    private LocalDateTime lastSeen;
}
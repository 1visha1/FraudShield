package com.fraudshield.risk.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "risk_assessments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID transactionId;

    private UUID customerId;

    private Integer riskScore;

    private String riskLevel;

    private LocalDateTime assessedAt;
}
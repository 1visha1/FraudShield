package com.fraudshield.risk.entity;

import jakarta.persistence.Column;
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
@Table(name = "fraud_rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudRule {

    @Id
    @GeneratedValue
    private UUID ruleId;

    private String ruleName;

    private Integer ruleVersion;

    @Column(length = 2000)
    private String ruleExpression;

    private Integer riskScore;

    private Boolean enabled;

    private LocalDateTime createdAt;
}
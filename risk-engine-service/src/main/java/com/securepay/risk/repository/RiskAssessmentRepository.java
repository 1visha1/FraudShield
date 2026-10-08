package com.fraudshield.risk.repository;
import com.fraudshield.risk.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RiskAssessmentRepository
        extends JpaRepository<RiskAssessment, UUID> {
}
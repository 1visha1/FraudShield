package com.fraudshield.fraud.repository;
import com.fraudshield.fraud.entity.FraudDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
public interface FraudDecisionRepository
        extends JpaRepository<FraudDecision, UUID> {
}
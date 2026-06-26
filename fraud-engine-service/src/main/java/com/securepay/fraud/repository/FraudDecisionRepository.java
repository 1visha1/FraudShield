package com.securepay.fraud.repository;
import com.securepay.fraud.entity.FraudDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
public interface FraudDecisionRepository
        extends JpaRepository<FraudDecision, UUID> {
}
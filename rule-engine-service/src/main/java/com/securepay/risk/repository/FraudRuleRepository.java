package com.securepay.risk.repository;

import com.securepay.risk.entity.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
public interface FraudRuleRepository
        extends JpaRepository<FraudRule, UUID> {

    List<FraudRule> findByEnabledTrue();
}
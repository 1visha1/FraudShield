package com.securepay.rule.repository;

import com.securepay.rule.entity.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
public interface FraudRuleRepository
        extends JpaRepository<FraudRule, UUID> {

    List<FraudRule> findByEnabledTrue();
}
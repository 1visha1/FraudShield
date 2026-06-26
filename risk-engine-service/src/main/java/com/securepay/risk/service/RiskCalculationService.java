package com.securepay.risk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskCalculationService {

    private final DeviceRiskCache deviceCache;

    public Integer calculateRisk(BigDecimal amount, UUID customerId) {
        log.info("Starting risk calculation for customer: {} with amount: {}", customerId, amount);
        
        int score = 0;
        if (amount.compareTo(BigDecimal.valueOf(50000)) > 0) {
            log.debug("Amount > 50,000. Adding 40 to risk score.");
            score += 40;
        }
        if (amount.compareTo(BigDecimal.valueOf(100000)) > 0) {
            log.debug("Amount > 100,000. Adding 20 to risk score.");
            score += 20;
        }

        Integer deviceRisk = deviceCache.get(customerId);
        log.debug("Device risk score for customer {}: {}", customerId, deviceRisk);
        score += deviceRisk;

        int finalScore = Math.min(score, 100);
        log.info("Final risk score for customer {}: {}", customerId, finalScore);
        return finalScore;
    }

    public String riskLevel(Integer score) {
        String level = (score < 30) ? "LOW" : (score < 70) ? "MEDIUM" : (score < 90) ? "HIGH" : "CRITICAL";
        log.info("Risk level determined as: {} for score: {}", level, score);
        return level;
    }
}
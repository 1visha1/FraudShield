package com.securepay.fraud.service;
import org.springframework.stereotype.Service;
@Service
public class FraudScoringService {

    public Integer calculate(
            Integer riskScore,
            Integer ruleScore) {

        return riskScore + ruleScore;
    }

    public String decision(
            Integer score) {

        if(score <= 30)
            return "APPROVE";

        if(score <= 70)
            return "STEP_UP_AUTH";

        if(score <= 100)
            return "HIGH_RISK";

        return "BLOCK";
    }
}

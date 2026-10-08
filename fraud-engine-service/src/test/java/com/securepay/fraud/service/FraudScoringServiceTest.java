package com.fraudshield.fraud.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FraudScoringServiceTest {

    private FraudScoringService fraudScoringService;

    @BeforeEach
    void setUp() {
        fraudScoringService = new FraudScoringService();
    }

    @Test
    void shouldCalculateFraudScore() {
        Integer score = fraudScoringService.calculate(40, 30);

        assertEquals(70, score);
    }

    @Test
    void shouldReturnApproveWhenScoreIsLessThanOrEqualTo30() {
        assertEquals("APPROVE", fraudScoringService.decision(0));
        assertEquals("APPROVE", fraudScoringService.decision(15));
        assertEquals("APPROVE", fraudScoringService.decision(30));
    }

    @Test
    void shouldReturnStepUpAuthWhenScoreIsBetween31And70() {
        assertEquals("STEP_UP_AUTH", fraudScoringService.decision(31));
        assertEquals("STEP_UP_AUTH", fraudScoringService.decision(50));
        assertEquals("STEP_UP_AUTH", fraudScoringService.decision(70));
    }

    @Test
    void shouldReturnHighRiskWhenScoreIsBetween71And100() {
        assertEquals("HIGH_RISK", fraudScoringService.decision(71));
        assertEquals("HIGH_RISK", fraudScoringService.decision(85));
        assertEquals("HIGH_RISK", fraudScoringService.decision(100));
    }

    @Test
    void shouldReturnBlockWhenScoreIsGreaterThan100() {
        assertEquals("BLOCK", fraudScoringService.decision(101));
        assertEquals("BLOCK", fraudScoringService.decision(150));
        assertEquals("BLOCK", fraudScoringService.decision(Integer.MAX_VALUE));
    }

    @Test
    void shouldCalculateZeroScore() {
        Integer score = fraudScoringService.calculate(0, 0);

        assertEquals(0, score);
    }

    @Test
    void shouldCalculateNegativeScores() {
        Integer score = fraudScoringService.calculate(-10, 20);

        assertEquals(10, score);
    }
}
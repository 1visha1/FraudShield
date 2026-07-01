package com.securepay.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpGeneratorTest {

    private OtpGenerator otpGenerator;

    @BeforeEach
    void setUp() {
        otpGenerator = new OtpGenerator();
    }

    @Test
    void generate_ShouldReturnSixDigitOtp() {

        String otp = otpGenerator.generate();

        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));

        int value = Integer.parseInt(otp);
        assertTrue(value >= 100000);
        assertTrue(value <= 999999);
    }

    @Test
    void generate_ShouldGenerateValidOtpMultipleTimes() {

        for (int i = 0; i < 100; i++) {

            String otp = otpGenerator.generate();

            assertNotNull(otp);
            assertEquals(6, otp.length());
            assertTrue(otp.matches("\\d{6}"));

            int value = Integer.parseInt(otp);
            assertTrue(value >= 100000);
            assertTrue(value <= 999999);
        }
    }

    @Test
    void generate_ShouldGenerateDifferentOtps() {

        String otp1 = otpGenerator.generate();
        String otp2 = otpGenerator.generate();

        assertNotNull(otp1);
        assertNotNull(otp2);

        assertEquals(6, otp1.length());
        assertEquals(6, otp2.length());

        // Extremely unlikely to fail (1 in 900,000 chance)
        assertNotEquals(otp1, otp2);
    }
}
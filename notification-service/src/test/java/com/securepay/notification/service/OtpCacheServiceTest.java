package com.securepay.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private OtpCacheService otpCacheService;

    @BeforeEach
    void setUp() {
        otpCacheService = new OtpCacheService(redisTemplate);
    }

    @Test
    void save_ShouldStoreOtpInRedis() {

        UUID transactionId = UUID.randomUUID();
        String otp = "123456";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        otpCacheService.save(transactionId, otp);

        verify(redisTemplate).opsForValue();

        verify(valueOperations).set(
                eq("otp:" + transactionId),
                eq(otp),
                eq(Duration.ofMinutes(5))
        );
    }

    @Test
    void get_ShouldReturnOtp_WhenPresent() {

        UUID transactionId = UUID.randomUUID();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:" + transactionId))
                .thenReturn("654321");

        String result = otpCacheService.get(transactionId);

        assertEquals("654321", result);

        verify(valueOperations).get("otp:" + transactionId);
    }

    @Test
    void get_ShouldReturnNull_WhenOtpDoesNotExist() {

        UUID transactionId = UUID.randomUUID();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:" + transactionId))
                .thenReturn(null);

        String result = otpCacheService.get(transactionId);

        assertNull(result);

        verify(valueOperations).get("otp:" + transactionId);
    }

    @Test
    void delete_ShouldRemoveOtpFromRedis() {

        UUID transactionId = UUID.randomUUID();

        otpCacheService.delete(transactionId);

        verify(redisTemplate).delete("otp:" + transactionId);
    }
}
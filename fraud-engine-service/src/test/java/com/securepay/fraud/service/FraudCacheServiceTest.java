package com.securepay.fraud.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private FraudCacheService fraudCacheService;

    private UUID customerId;
    private String expectedKey;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        expectedKey = "velocity:user:" + customerId;
    }

    @Test
    void shouldIncrementCounterAndSetExpiryForFirstTransaction() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(expectedKey)).thenReturn(1L);

        long result = fraudCacheService.getUserTransactionVelocity(customerId);

        assertEquals(1L, result);

        verify(valueOperations).increment(expectedKey);
        verify(redisTemplate).expire(expectedKey, Duration.ofHours(1));
    }

    @Test
    void shouldIncrementCounterWithoutSettingExpiryForSubsequentTransactions() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(expectedKey)).thenReturn(5L);

        long result = fraudCacheService.getUserTransactionVelocity(customerId);

        assertEquals(5L, result);

        verify(valueOperations).increment(expectedKey);
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void shouldReturnZeroWhenIncrementReturnsNull() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(expectedKey)).thenReturn(null);

        long result = fraudCacheService.getUserTransactionVelocity(customerId);

        assertEquals(0L, result);

        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }
}
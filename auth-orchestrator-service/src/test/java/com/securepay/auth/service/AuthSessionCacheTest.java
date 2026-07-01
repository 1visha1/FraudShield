package com.securepay.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthSessionCacheTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AuthSessionCache authSessionCache;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void save_ShouldStoreSessionInRedis() {

        // Arrange
        UUID transactionId = UUID.randomUUID();
        String authType = "SMS_OTP";

        // Act
        authSessionCache.save(transactionId, authType);

        // Assert
        verify(redisTemplate, times(1)).opsForValue();

        verify(valueOperations, times(1)).set(
                "session:" + transactionId,
                authType,
                Duration.ofMinutes(10)
        );

        verifyNoMoreInteractions(redisTemplate, valueOperations);
    }

    @Test
    void save_ShouldStoreDifferentAuthType() {

        // Arrange
        UUID transactionId = UUID.randomUUID();
        String authType = "SMS_EMAIL_OTP";

        // Act
        authSessionCache.save(transactionId, authType);

        // Assert
        verify(valueOperations).set(
                "session:" + transactionId,
                "SMS_EMAIL_OTP",
                Duration.ofMinutes(10)
        );
    }
}
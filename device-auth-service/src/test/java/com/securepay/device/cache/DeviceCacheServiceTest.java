package com.fraudshield.device.cache;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private DeviceCacheService deviceCacheService;

    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);
    }

    @Test
    void shouldCacheDeviceRiskScore() {

        Integer score = 50;

        deviceCacheService.cache(customerId, score);

        verify(valueOperations).set(
                "device:" + customerId,
                score,
                Duration.ofHours(24));

        verifyNoMoreInteractions(valueOperations);
    }

    @Test
    void shouldReturnCachedRiskScore() {

        when(valueOperations.get("device:" + customerId))
                .thenReturn(50);

        Integer result = deviceCacheService.get(customerId);

        assertNotNull(result);
        assertEquals(50, result);

        verify(valueOperations).get("device:" + customerId);
    }

    @Test
    void shouldReturnNullWhenCacheDoesNotExist() {

        when(valueOperations.get("device:" + customerId))
                .thenReturn(null);

        Integer result = deviceCacheService.get(customerId);

        assertNull(result);

        verify(valueOperations).get("device:" + customerId);
    }

    @Test
    void shouldReturnDifferentScoresForDifferentCustomers() {

        UUID customer1 = UUID.randomUUID();
        UUID customer2 = UUID.randomUUID();

        when(valueOperations.get("device:" + customer1))
                .thenReturn(25);

        when(valueOperations.get("device:" + customer2))
                .thenReturn(80);

        assertEquals(25, deviceCacheService.get(customer1));
        assertEquals(80, deviceCacheService.get(customer2));

        verify(valueOperations).get("device:" + customer1);
        verify(valueOperations).get("device:" + customer2);
    }
}
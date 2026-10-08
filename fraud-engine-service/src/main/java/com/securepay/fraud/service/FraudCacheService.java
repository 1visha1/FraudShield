package com.fraudshield.fraud.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FraudCacheService {

    private final StringRedisTemplate redisTemplate;

    private static final String VELOCITY_KEY_PREFIX = "velocity:user:";
    private static final Duration VELOCITY_WINDOW = Duration.ofHours(1);

    public long getUserTransactionVelocity(UUID customerId) {
        String key = VELOCITY_KEY_PREFIX + customerId.toString();
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            // First transaction in the window, set the expiry
            redisTemplate.expire(key, VELOCITY_WINDOW);
        }
        return count != null ? count : 0;
    }
}
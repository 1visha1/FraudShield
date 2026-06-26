package com.securepay.risk.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceRiskCache {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration CACHE_TTL = Duration.ofHours(24);

    public void put(UUID customerId, Integer riskScore) {

        redisTemplate.opsForValue().set(
                "device:" + customerId,
                riskScore,
                CACHE_TTL
        );
    }

    public Integer get(UUID customerId) {

        Object value = redisTemplate.opsForValue()
                .get("device:" + customerId);

        if (value == null) {
            return 50; // Default medium risk
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        return Integer.parseInt(value.toString());
    }

    public void delete(UUID customerId) {

        redisTemplate.delete("device:" + customerId);
    }

    public boolean exists(UUID customerId) {

        Boolean exists = redisTemplate.hasKey("device:" + customerId);

        return Boolean.TRUE.equals(exists);
    }
}
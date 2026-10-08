package com.fraudshield.device.cache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceCacheService {

    private final RedisTemplate<String,Object>
            redisTemplate;

    public void cache(
            UUID customerId,
            Integer score) {

        redisTemplate.opsForValue().set(
                "device:"+customerId,
                score,
                Duration.ofHours(24));
    }

    public Integer get(
            UUID customerId) {

        Object value =
                redisTemplate.opsForValue()
                        .get(
                                "device:"+customerId);

        return value == null
                ? null
                : (Integer)value;
    }
}
package com.fraudshield.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthSessionCache {

    private final RedisTemplate<String,Object>
            redisTemplate;

    public void save(
            UUID transactionId,
            String authType) {

        redisTemplate.opsForValue().set(
                "session:"+transactionId,
                authType,
                Duration.ofMinutes(10));
    }
}
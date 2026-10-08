package com.fraudshield.notification.service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class OtpCacheService {

    private final RedisTemplate<String,Object>
            redisTemplate;

    public void save(
            UUID transactionId,
            String otp) {

        redisTemplate.opsForValue().set(
                "otp:"+transactionId,
                otp,
                Duration.ofMinutes(5));
    }

    public String get(
            UUID transactionId){

        Object value =
                redisTemplate.opsForValue()
                        .get(
                                "otp:"+transactionId);

        return value == null
                ? null
                : value.toString();
    }

    public void delete(
            UUID transactionId){

        redisTemplate.delete(
                "otp:"+transactionId);
    }
}
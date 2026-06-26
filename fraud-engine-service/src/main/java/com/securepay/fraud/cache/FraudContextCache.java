package com.securepay.fraud.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudContextCache {

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveRisk(UUID txn, UUID customerId, Integer score) {
        String key = "fraud:" + txn;
        redisTemplate.opsForHash().put(key, "riskScore", score);
        redisTemplate.opsForHash().put(key, "customerId", customerId.toString());
        redisTemplate.expire(key, Duration.ofMinutes(10));
        log.info("Saved risk score for txn {}: {}", txn, score);
    }

    public void saveRule(UUID txn, UUID customerId, Integer score, List<String> matchedRules) {
        String key = "fraud:" + txn;
        redisTemplate.opsForHash().put(key, "ruleScore", score);
        redisTemplate.opsForHash().put(key, "customerId", customerId.toString());
        if (matchedRules != null && !matchedRules.isEmpty()) {
            redisTemplate.opsForHash().put(key, "matchedRules", String.join(",", matchedRules));
        }
        redisTemplate.expire(key, Duration.ofMinutes(10));
        log.info("Saved rule score for txn {}: {}", txn, score);
    }

    public Integer risk(UUID txn) {
        return getAsInteger("fraud:" + txn, "riskScore");
    }

    public Integer rule(UUID txn) {
        return getAsInteger("fraud:" + txn, "ruleScore");
    }

    private Integer getAsInteger(String key, String field) {
        Object value = redisTemplate.opsForHash().get(key, field);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    public UUID customerId(UUID txn) {
        Object value = redisTemplate.opsForHash().get("fraud:" + txn, "customerId");
        return value == null ? null : UUID.fromString((String) value);
    }

    public List<String> matchedRules(UUID txn) {
        Object value = redisTemplate.opsForHash().get("fraud:" + txn, "matchedRules");
        if (value == null) return Collections.emptyList();
        return Arrays.asList(((String) value).split(","));
    }

    public boolean ready(UUID txn) {
        Integer risk = risk(txn);
        Integer rule = rule(txn);
        boolean isReady = risk != null && rule != null;
        log.info("Checking readiness for txn {}: risk={}, rule={}, ready={}", txn, risk, rule, isReady);
        return isReady;
    }
}
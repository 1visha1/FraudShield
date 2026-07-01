package com.securepay.fraud.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudContextCacheTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private FraudContextCache fraudContextCache;

    private UUID txnId;
    private UUID customerId;
    private String key;

    @BeforeEach
    void setUp() {
        txnId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        key = "fraud:" + txnId;

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void shouldSaveRisk() {

        fraudContextCache.saveRisk(txnId, customerId, 80);

        verify(hashOperations).put(key, "riskScore", 80);
        verify(hashOperations).put(key, "customerId", customerId.toString());
        verify(redisTemplate).expire(key, Duration.ofMinutes(10));
    }

    @Test
    void shouldSaveRuleWithMatchedRules() {

        List<String> rules = List.of("RULE1", "RULE2");

        fraudContextCache.saveRule(txnId, customerId, 50, rules);

        verify(hashOperations).put(key, "ruleScore", 50);
        verify(hashOperations).put(key, "customerId", customerId.toString());
        verify(hashOperations).put(key, "matchedRules", "RULE1,RULE2");
        verify(redisTemplate).expire(key, Duration.ofMinutes(10));
    }

    @Test
    void shouldSaveRuleWithoutMatchedRules() {

        fraudContextCache.saveRule(txnId, customerId, 40, List.of());

        verify(hashOperations).put(key, "ruleScore", 40);
        verify(hashOperations).put(key, "customerId", customerId.toString());

        verify(hashOperations, never())
                .put(eq(key), eq("matchedRules"), any());

        verify(redisTemplate).expire(key, Duration.ofMinutes(10));
    }

    @Test
    void shouldReturnRiskScore() {

        when(hashOperations.get(key, "riskScore")).thenReturn(75);

        Integer score = fraudContextCache.risk(txnId);

        assertEquals(75, score);
    }

    @Test
    void shouldReturnRuleScore() {

        when(hashOperations.get(key, "ruleScore")).thenReturn(60);

        Integer score = fraudContextCache.rule(txnId);

        assertEquals(60, score);
    }

    @Test
    void shouldReturnNullWhenRiskScoreMissing() {

        when(hashOperations.get(key, "riskScore")).thenReturn(null);

        assertNull(fraudContextCache.risk(txnId));
    }

    @Test
    void shouldReturnCustomerId() {

        when(hashOperations.get(key, "customerId"))
                .thenReturn(customerId.toString());

        UUID result = fraudContextCache.customerId(txnId);

        assertEquals(customerId, result);
    }

    @Test
    void shouldReturnNullCustomerId() {

        when(hashOperations.get(key, "customerId"))
                .thenReturn(null);

        assertNull(fraudContextCache.customerId(txnId));
    }

    @Test
    void shouldReturnMatchedRules() {

        when(hashOperations.get(key, "matchedRules"))
                .thenReturn("RULE1,RULE2,RULE3");

        List<String> rules = fraudContextCache.matchedRules(txnId);

        assertEquals(3, rules.size());
        assertEquals("RULE1", rules.get(0));
        assertEquals("RULE2", rules.get(1));
        assertEquals("RULE3", rules.get(2));
    }

    @Test
    void shouldReturnEmptyMatchedRules() {

        when(hashOperations.get(key, "matchedRules"))
                .thenReturn(null);

        List<String> rules = fraudContextCache.matchedRules(txnId);

        assertTrue(rules.isEmpty());
    }

    @Test
    void shouldReturnReadyWhenRiskAndRuleExist() {

        when(hashOperations.get(key, "riskScore")).thenReturn(90);
        when(hashOperations.get(key, "ruleScore")).thenReturn(40);

        assertTrue(fraudContextCache.ready(txnId));
    }

    @Test
    void shouldReturnFalseWhenRiskMissing() {

        when(hashOperations.get(key, "riskScore")).thenReturn(null);
        when(hashOperations.get(key, "ruleScore")).thenReturn(40);

        assertFalse(fraudContextCache.ready(txnId));
    }

    @Test
    void shouldReturnFalseWhenRuleMissing() {

        when(hashOperations.get(key, "riskScore")).thenReturn(40);
        when(hashOperations.get(key, "ruleScore")).thenReturn(null);

        assertFalse(fraudContextCache.ready(txnId));
    }
}
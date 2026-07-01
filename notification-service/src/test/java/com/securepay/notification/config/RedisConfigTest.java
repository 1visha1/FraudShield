package com.securepay.notification.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RedisConfigTest {

    private final RedisConfig redisConfig = new RedisConfig();

    @Test
    void redisTemplate_ShouldCreateRedisTemplate() {

        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> template =
                redisConfig.redisTemplate(factory);

        assertNotNull(template);
        assertSame(factory, template.getConnectionFactory());
    }

    @Test
    void redisTemplate_ShouldReturnRedisTemplateInstance() {

        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> template =
                redisConfig.redisTemplate(factory);

        assertTrue(template instanceof RedisTemplate);
    }
}
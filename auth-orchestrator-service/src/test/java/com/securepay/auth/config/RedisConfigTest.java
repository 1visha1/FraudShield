package com.securepay.auth.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RedisConfigTest {

    private RedisConfig redisConfig;

    @BeforeEach
    void setUp() {
        redisConfig = new RedisConfig();
    }

    @Test
    void redisTemplate_ShouldCreateRedisTemplate() {

        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> template =
                redisConfig.redisTemplate(factory);

        assertNotNull(template);
        assertSame(factory, template.getConnectionFactory());
    }

    @Test
    void redisTemplate_ShouldReturnNewInstanceEachTime() {

        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> template1 =
                redisConfig.redisTemplate(factory);

        RedisTemplate<String, Object> template2 =
                redisConfig.redisTemplate(factory);

        assertNotSame(template1, template2);
    }

    @Test
    void redisTemplate_ShouldUseProvidedConnectionFactory() {

        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> template =
                redisConfig.redisTemplate(factory);

        assertEquals(factory, template.getConnectionFactory());
    }
}
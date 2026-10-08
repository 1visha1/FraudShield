package com.fraudshield.device.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisConfigTest {

    private final RedisConfig redisConfig = new RedisConfig();

    @Test
    void shouldCreateRedisTemplate() {

        RedisConnectionFactory connectionFactory =
                mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> template =
                redisConfig.redisTemplate(connectionFactory);

        assertNotNull(template);
        assertSame(connectionFactory, template.getConnectionFactory());
    }

    @Test
    void shouldReturnNewRedisTemplateInstanceEachTime() {

        RedisConnectionFactory connectionFactory =
                mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> first =
                redisConfig.redisTemplate(connectionFactory);

        RedisTemplate<String, Object> second =
                redisConfig.redisTemplate(connectionFactory);

        assertNotNull(first);
        assertNotNull(second);
        assertNotSame(first, second);
    }

    @Test
    void shouldUseProvidedConnectionFactory() {

        RedisConnectionFactory connectionFactory =
                mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> template =
                redisConfig.redisTemplate(connectionFactory);

        assertEquals(connectionFactory,
                template.getConnectionFactory());
    }
}
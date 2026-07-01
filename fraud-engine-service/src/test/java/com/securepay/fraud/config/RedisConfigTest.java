package com.securepay.fraud.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RedisConfigTest {

    private final RedisConfig redisConfig = new RedisConfig();

    @Test
    void shouldCreateRedisTemplate() {

        RedisConnectionFactory connectionFactory =
                mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> template =
                redisConfig.redisTemplate(connectionFactory);

        assertNotNull(template);
        assertEquals(connectionFactory, template.getConnectionFactory());
    }

    @Test
    void shouldUseStringSerializerForKeys() {

        RedisConnectionFactory connectionFactory =
                mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> template =
                redisConfig.redisTemplate(connectionFactory);

        assertTrue(
                template.getKeySerializer() instanceof StringRedisSerializer);

        assertTrue(
                template.getHashKeySerializer() instanceof StringRedisSerializer);
    }

    @Test
    void shouldUseJsonSerializerForValues() {

        RedisConnectionFactory connectionFactory =
                mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> template =
                redisConfig.redisTemplate(connectionFactory);

        assertTrue(
                template.getValueSerializer()
                        instanceof GenericJackson2JsonRedisSerializer);

        assertTrue(
                template.getHashValueSerializer()
                        instanceof GenericJackson2JsonRedisSerializer);
    }
}
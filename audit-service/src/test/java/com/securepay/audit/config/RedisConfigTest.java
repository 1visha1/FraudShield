package com.fraudshield.audit.config;

import com.fraudshield.audit.service.RedisStreamConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisConfigTest {

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Mock
    private RedisStreamConsumer redisStreamConsumer;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @SuppressWarnings("unchecked")
    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @Mock
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;

    @Mock
    private Subscription subscription;

    private final RedisConfig redisConfig = new RedisConfig();

    @Test
    void testRedisTemplateConfiguration() {

        RedisTemplate<String, Object> template =
                redisConfig.redisTemplate(connectionFactory);

        assertNotNull(template);
        assertEquals(connectionFactory, template.getConnectionFactory());

        assertNotNull(template.getKeySerializer());
        assertNotNull(template.getHashKeySerializer());
        assertNotNull(template.getValueSerializer());
        assertNotNull(template.getHashValueSerializer());
    }

    @Test
    void testSubscriptionCreatesGroupAndStartsListener() {

        when(stringRedisTemplate.hasKey("audit-stream")).thenReturn(false);
        when(stringRedisTemplate.opsForStream()).thenReturn(streamOperations);

        when(streamOperations.add(anyString(), anyMap())).thenReturn(null);

        // createGroup() is NOT void
        when(streamOperations.createGroup(anyString(), anyString()))
                .thenReturn("audit-group");

        try (MockedStatic<StreamMessageListenerContainer> mockedStatic =
                     mockStatic(StreamMessageListenerContainer.class)) {

            mockedStatic.when(() ->
                            StreamMessageListenerContainer.create(
                                    eq(connectionFactory),
                                    any(StreamMessageListenerContainer.StreamMessageListenerContainerOptions.class)))
                    .thenReturn(listenerContainer);

            when(listenerContainer.receive(
                    any(),
                    any(),
                    eq(redisStreamConsumer)))
                    .thenReturn(subscription);

            Subscription result = redisConfig.subscription(
                    connectionFactory,
                    redisStreamConsumer,
                    stringRedisTemplate);

            assertNotNull(result);
            assertEquals(subscription, result);

            verify(stringRedisTemplate).hasKey("audit-stream");
            verify(streamOperations).add(eq("audit-stream"), anyMap());
            verify(streamOperations).createGroup("audit-stream", "audit-group");

            verify(listenerContainer).receive(
                    any(),
                    any(),
                    eq(redisStreamConsumer));

            verify(listenerContainer).start();
        }
    }

    @Test
    void testSubscriptionWhenConsumerGroupAlreadyExists() {

        when(stringRedisTemplate.hasKey("audit-stream")).thenReturn(true);
        when(stringRedisTemplate.opsForStream()).thenReturn(streamOperations);

        // Simulate BUSYGROUP exception
        when(streamOperations.createGroup(anyString(), anyString()))
                .thenThrow(new RuntimeException("BUSYGROUP"));

        try (MockedStatic<StreamMessageListenerContainer> mockedStatic =
                     mockStatic(StreamMessageListenerContainer.class)) {

            mockedStatic.when(() ->
                            StreamMessageListenerContainer.create(
                                    eq(connectionFactory),
                                    any(StreamMessageListenerContainer.StreamMessageListenerContainerOptions.class)))
                    .thenReturn(listenerContainer);

            when(listenerContainer.receive(
                    any(),
                    any(),
                    eq(redisStreamConsumer)))
                    .thenReturn(subscription);

            Subscription result = redisConfig.subscription(
                    connectionFactory,
                    redisStreamConsumer,
                    stringRedisTemplate);

            assertNotNull(result);

            verify(listenerContainer).receive(
                    any(),
                    any(),
                    eq(redisStreamConsumer));

            verify(listenerContainer).start();
        }
    }
}
package com.fraudshield.audit.config;

import com.fraudshield.audit.service.RedisStreamConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public Subscription subscription(RedisConnectionFactory redisConnectionFactory, RedisStreamConsumer redisStreamConsumer, org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate) {
        
        try {
            if (Boolean.FALSE.equals(stringRedisTemplate.hasKey("audit-stream"))) {
                stringRedisTemplate.opsForStream().add("audit-stream", java.util.Collections.singletonMap("init", "true"));
            }
            stringRedisTemplate.opsForStream().createGroup("audit-stream", "audit-group");
        } catch (Exception e) {
            // Group may already exist
        }

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options = StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofSeconds(1))
                .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer = StreamMessageListenerContainer.create(redisConnectionFactory, options);

        Subscription subscription = listenerContainer.receive(
                Consumer.from("audit-group", "audit-consumer"),
                StreamOffset.create("audit-stream", ReadOffset.lastConsumed()),
                redisStreamConsumer);

        listenerContainer.start();
        return subscription;
    }
}
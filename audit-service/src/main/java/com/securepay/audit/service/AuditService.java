package com.fraudshield.audit.service;

import com.fraudshield.audit.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final StringRedisTemplate redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void auditEvent(String event) {
        log.info("Auditing event: {}", event);
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("event", event);
        redisTemplate.opsForStream().add("audit-stream", body);
    }
}
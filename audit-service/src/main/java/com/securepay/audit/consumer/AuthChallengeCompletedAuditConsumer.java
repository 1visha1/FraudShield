package com.securepay.audit.consumer;

import com.securepay.audit.event.AuthChallengeCompletedEvent;
import com.securepay.audit.service.AuditWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthChallengeCompletedAuditConsumer {

    private final AuditWriter writer;

    @RabbitListener(queues = "audit.auth.challenge.completed.q")
    public void consume(AuthChallengeCompletedEvent event) {
        log.info("Auditing AuthChallengeCompletedEvent for transaction: {}", event.getTransactionId());
        writer.write("AUTH_CHALLENGE_COMPLETED", "auth-orchestrator", event);
    }
}
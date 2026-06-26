package com.securepay.audit.consumer;

import com.securepay.audit.event.AuthChallengeSentEvent;
import com.securepay.audit.service.AuditWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OtpAuditConsumer {

    private final AuditWriter writer;

    @RabbitListener(queues = "auth.challenge.created.q")
    public void consume(AuthChallengeSentEvent event) {
        writer.write("OTP_SENT", "notification-service", event);
    }
}
package com.securepay.audit.consumer;

import com.securepay.audit.event.DeviceVerifiedEvent;
import com.securepay.audit.service.AuditWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceAuditConsumer {

    private final AuditWriter writer;

    @RabbitListener(queues = "device.verified.q")
    public void consume(DeviceVerifiedEvent event) {
        log.info("event:{}",event);
        writer.write("DEVICE_VERIFIED", "device-service", event);
    }
}
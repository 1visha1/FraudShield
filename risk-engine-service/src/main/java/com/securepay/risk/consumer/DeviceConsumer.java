package com.fraudshield.risk.consumer;
import com.fraudshield.risk.event.DeviceVerifiedEvent;
import com.fraudshield.risk.service.DeviceRiskCache;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceConsumer {

    private final DeviceRiskCache cache;

    @RabbitListener(
            queues = "device.verified.q")
    public void consume(
            DeviceVerifiedEvent event) {

        cache.put(
                event.getCustomerId(),
                event.getDeviceRiskScore());
    }
}
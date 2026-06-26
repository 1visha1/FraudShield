package com.securepay.risk.consumer;
import com.securepay.risk.event.DeviceVerifiedEvent;
import com.securepay.risk.service.DeviceRiskCache;
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
package com.fraudshield.device.service;

import com.fraudshield.device.cache.DeviceCacheService;
import com.fraudshield.device.dto.*;
import com.fraudshield.device.entity.TrustedDevice;
import com.fraudshield.device.event.DeviceVerifiedEvent;
import com.fraudshield.device.repository.TrustedDeviceRepository;
import com.fraudshield.device.util.FingerprintUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.fraudshield.device.config.RabbitMQConfig.*;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final TrustedDeviceRepository repository;

    private final RabbitTemplate rabbitTemplate;

    private final DeviceCacheService deviceCacheService;

    public DeviceVerificationResponse verify(
            DeviceVerificationRequest request) {

        String fingerprint =
                FingerprintUtil.generate(
                        request.getDeviceId().toString(),
                        request.getUserAgent(),
                        request.getIpAddress(),
                        request.getTimezone(),
                        request.getOsVersion());

        TrustedDevice existing =
                repository.findById(
                                request.getDeviceId())
                        .orElse(null);

        boolean trusted;

        int riskScore;

        if (existing == null) {

            trusted = false;
            riskScore = 50;

            TrustedDevice device =
                    TrustedDevice.builder()
                            .deviceId(
                                    request.getDeviceId())
                            .customerId(
                                    request.getCustomerId())
                            .fingerprint(
                                    fingerprint)
                            .riskScore(
                                    riskScore)
                            .trusted(false)
                            .createdAt(
                                    LocalDateTime.now())
                            .lastSeen(
                                    LocalDateTime.now())
                            .build();

            repository.save(device);

        } else {

            trusted = true;
            riskScore = 10;

            existing.setLastSeen(
                    LocalDateTime.now());

            repository.save(existing);
        }

        DeviceVerifiedEvent event =
                DeviceVerifiedEvent.builder()
                        .eventType(
                                "DEVICE_VERIFIED")
                        .customerId(
                                request.getCustomerId())
                        .deviceId(
                                request.getDeviceId())
                        .trusted(trusted)
                        .deviceRiskScore(
                                riskScore)
                        .build();

        deviceCacheService.cache(
                request.getCustomerId(),
                riskScore
        );

        rabbitTemplate.convertAndSend(
                EXCHANGE,
                ROUTING_KEY,
                event);

        return DeviceVerificationResponse
                .builder()
                .trusted(trusted)
                .deviceRiskScore(riskScore)
                .fingerprint(fingerprint)
                .build();
    }
}
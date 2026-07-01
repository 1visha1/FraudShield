package com.securepay.device.service;

import com.securepay.device.cache.DeviceCacheService;
import com.securepay.device.dto.DeviceVerificationRequest;
import com.securepay.device.dto.DeviceVerificationResponse;
import com.securepay.device.entity.TrustedDevice;
import com.securepay.device.event.DeviceVerifiedEvent;
import com.securepay.device.repository.TrustedDeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.securepay.device.config.RabbitMQConfig.EXCHANGE;
import static com.securepay.device.config.RabbitMQConfig.ROUTING_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private TrustedDeviceRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private DeviceCacheService deviceCacheService;

    @InjectMocks
    private DeviceService deviceService;

    private DeviceVerificationRequest request;

    @BeforeEach
    void setup() {

        request = new DeviceVerificationRequest();

        request.setCustomerId(UUID.randomUUID());
        request.setDeviceId(UUID.randomUUID());
        request.setUserAgent("Chrome");
        request.setIpAddress("192.168.1.1");
        request.setTimezone("Asia/Kolkata");
        request.setOsVersion("Windows 11");
    }

    @Test
    void shouldCreateNewDeviceWhenDeviceDoesNotExist() {

        when(repository.findById(request.getDeviceId()))
                .thenReturn(Optional.empty());

        when(repository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeviceVerificationResponse response =
                deviceService.verify(request);

        assertFalse(response.getTrusted());
        assertEquals(50, response.getDeviceRiskScore());
        assertNotNull(response.getFingerprint());

        verify(repository).save(any(TrustedDevice.class));

        verify(deviceCacheService)
                .cache(request.getCustomerId(), 50);

        verify(rabbitTemplate)
                .convertAndSend(
                        eq(EXCHANGE),
                        eq(ROUTING_KEY),
                        any(DeviceVerifiedEvent.class));
    }

    @Test
    void shouldReturnTrustedDeviceWhenDeviceExists() {

        TrustedDevice existing =
                TrustedDevice.builder()
                        .deviceId(request.getDeviceId())
                        .customerId(request.getCustomerId())
                        .trusted(true)
                        .riskScore(10)
                        .createdAt(LocalDateTime.now())
                        .lastSeen(LocalDateTime.now())
                        .build();

        when(repository.findById(request.getDeviceId()))
                .thenReturn(Optional.of(existing));

        when(repository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeviceVerificationResponse response =
                deviceService.verify(request);

        assertTrue(response.getTrusted());
        assertEquals(10, response.getDeviceRiskScore());

        verify(repository).save(existing);

        verify(deviceCacheService)
                .cache(request.getCustomerId(), 10);

        verify(rabbitTemplate)
                .convertAndSend(
                        eq(EXCHANGE),
                        eq(ROUTING_KEY),
                        any(DeviceVerifiedEvent.class));
    }

    @Test
    void shouldPublishCorrectEvent() {

        when(repository.findById(any()))
                .thenReturn(Optional.empty());

        when(repository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        deviceService.verify(request);

        ArgumentCaptor<DeviceVerifiedEvent> captor =
                ArgumentCaptor.forClass(DeviceVerifiedEvent.class);

        verify(rabbitTemplate)
                .convertAndSend(
                        eq(EXCHANGE),
                        eq(ROUTING_KEY),
                        captor.capture());

        DeviceVerifiedEvent event = captor.getValue();

        assertEquals("DEVICE_VERIFIED", event.getEventType());
        assertEquals(request.getCustomerId(), event.getCustomerId());
        assertEquals(request.getDeviceId(), event.getDeviceId());
        assertFalse(event.getTrusted());
        assertEquals(50, event.getDeviceRiskScore());
    }

    @Test
    void shouldUpdateLastSeenForExistingDevice() {

        TrustedDevice existing =
                TrustedDevice.builder()
                        .deviceId(request.getDeviceId())
                        .customerId(request.getCustomerId())
                        .trusted(true)
                        .riskScore(10)
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .lastSeen(LocalDateTime.now().minusDays(1))
                        .build();

        when(repository.findById(request.getDeviceId()))
                .thenReturn(Optional.of(existing));

        when(repository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        deviceService.verify(request);

        verify(repository).save(existing);

        assertNotNull(existing.getLastSeen());
    }

    @Test
    void shouldGenerateFingerprint() {

        when(repository.findById(any()))
                .thenReturn(Optional.empty());

        when(repository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeviceVerificationResponse response =
                deviceService.verify(request);

        assertNotNull(response.getFingerprint());
        assertFalse(response.getFingerprint().isBlank());
    }
}
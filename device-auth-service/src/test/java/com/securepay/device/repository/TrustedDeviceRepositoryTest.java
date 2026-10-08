package com.fraudshield.device.repository;

import com.fraudshield.device.entity.TrustedDevice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TrustedDeviceRepositoryTest {

    @Autowired
    private TrustedDeviceRepository repository;

    @Test
    void shouldSaveDevice() {

        UUID deviceId = UUID.randomUUID();

        TrustedDevice device = TrustedDevice.builder()
                .deviceId(deviceId)
                .customerId(UUID.randomUUID())
                .fingerprint("fingerprint-123")
                .riskScore(50)
                .trusted(false)
                .createdAt(LocalDateTime.now())
                .lastSeen(LocalDateTime.now())
                .build();

        TrustedDevice saved = repository.save(device);

        assertNotNull(saved);
        assertEquals(deviceId, saved.getDeviceId());
    }

    @Test
    void shouldFindDeviceById() {

        UUID deviceId = UUID.randomUUID();

        TrustedDevice device = TrustedDevice.builder()
                .deviceId(deviceId)
                .customerId(UUID.randomUUID())
                .fingerprint("fingerprint-123")
                .riskScore(50)
                .trusted(false)
                .createdAt(LocalDateTime.now())
                .lastSeen(LocalDateTime.now())
                .build();

        repository.save(device);

        Optional<TrustedDevice> result =
                repository.findById(deviceId);

        assertTrue(result.isPresent());
        assertEquals(deviceId, result.get().getDeviceId());
    }

    @Test
    void shouldReturnEmptyWhenDeviceDoesNotExist() {

        Optional<TrustedDevice> result =
                repository.findById(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUpdateDevice() {

        UUID deviceId = UUID.randomUUID();

        TrustedDevice device = TrustedDevice.builder()
                .deviceId(deviceId)
                .customerId(UUID.randomUUID())
                .fingerprint("old-fingerprint")
                .riskScore(50)
                .trusted(false)
                .createdAt(LocalDateTime.now())
                .lastSeen(LocalDateTime.now())
                .build();

        repository.save(device);

        device.setTrusted(true);
        device.setRiskScore(10);

        repository.save(device);

        TrustedDevice updated =
                repository.findById(deviceId).orElseThrow();

        assertTrue(updated.getTrusted());
        assertEquals(10, updated.getRiskScore());
    }

    @Test
    void shouldDeleteDevice() {

        UUID deviceId = UUID.randomUUID();

        TrustedDevice device = TrustedDevice.builder()
                .deviceId(deviceId)
                .customerId(UUID.randomUUID())
                .fingerprint("fingerprint")
                .riskScore(50)
                .trusted(false)
                .createdAt(LocalDateTime.now())
                .lastSeen(LocalDateTime.now())
                .build();

        repository.save(device);

        repository.deleteById(deviceId);

        assertFalse(repository.findById(deviceId).isPresent());
    }

    @Test
    void shouldCountDevices() {

        long before = repository.count();

        TrustedDevice device = TrustedDevice.builder()
                .deviceId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .fingerprint("fp")
                .riskScore(25)
                .trusted(true)
                .createdAt(LocalDateTime.now())
                .lastSeen(LocalDateTime.now())
                .build();

        repository.save(device);

        assertEquals(before + 1, repository.count());
    }
}
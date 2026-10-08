package com.fraudshield.device.repository;

import com.fraudshield.device.entity.TrustedDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrustedDeviceRepository
        extends JpaRepository<TrustedDevice, UUID> {
}
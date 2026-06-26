package com.securepay.device.repository;

import com.securepay.device.entity.TrustedDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrustedDeviceRepository
        extends JpaRepository<TrustedDevice, UUID> {
}
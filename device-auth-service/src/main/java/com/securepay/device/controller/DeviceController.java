package com.fraudshield.device.controller;

import com.fraudshield.device.dto.*;
import com.fraudshield.device.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService service;

    @PostMapping("/verify")
    public DeviceVerificationResponse verify(
            @Valid
            @RequestBody
            DeviceVerificationRequest request) {

        return service.verify(request);
    }
}
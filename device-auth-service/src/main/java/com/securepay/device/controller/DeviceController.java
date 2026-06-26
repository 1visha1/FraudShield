package com.securepay.device.controller;

import com.securepay.device.dto.*;
import com.securepay.device.service.DeviceService;
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
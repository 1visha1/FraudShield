package com.securepay.device.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securepay.device.dto.DeviceVerificationRequest;
import com.securepay.device.dto.DeviceVerificationResponse;
import com.securepay.device.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceService deviceService;

    @Test
    void shouldVerifyDeviceSuccessfully() throws Exception {

        DeviceVerificationRequest request = new DeviceVerificationRequest();
        request.setCustomerId(UUID.randomUUID());
        request.setDeviceId(UUID.randomUUID());
        request.setUserAgent("Chrome");
        request.setIpAddress("192.168.1.10");
        request.setTimezone("Asia/Kolkata");
        request.setOsVersion("Windows 11");

        DeviceVerificationResponse response =
                DeviceVerificationResponse.builder()
                        .trusted(false)
                        .deviceRiskScore(50)
                        .fingerprint("abc123fingerprint")
                        .build();

        when(deviceService.verify(any(DeviceVerificationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/devices/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trusted").value(false))
                .andExpect(jsonPath("$.deviceRiskScore").value(50))
                .andExpect(jsonPath("$.fingerprint").value("abc123fingerprint"));
    }

    @Test
    void shouldReturnBadRequestForInvalidPayload() throws Exception {

        DeviceVerificationRequest request = new DeviceVerificationRequest();

        mockMvc.perform(post("/api/v1/devices/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
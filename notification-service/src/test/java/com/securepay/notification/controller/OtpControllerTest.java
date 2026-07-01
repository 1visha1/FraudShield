package com.securepay.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securepay.notification.dto.OtpVerificationRequest;
import com.securepay.notification.event.AuthChallengeCompletedEvent;
import com.securepay.notification.service.OtpCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OtpController.class)
@AutoConfigureMockMvc(addFilters = false)
class OtpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OtpCacheService cache;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should verify OTP successfully")
    void verify_ShouldReturnSuccess() throws Exception {

        UUID transactionId = UUID.randomUUID();

        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setTransactionId(transactionId);
        request.setOtp("123456");

        when(cache.get(transactionId)).thenReturn("123456");

        mockMvc.perform(post("/api/v1/notifications/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP verified successfully."))
                .andExpect(jsonPath("$.data.status").value("OTP_VERIFIED"));

        verify(cache).delete(transactionId);

        ArgumentCaptor<AuthChallengeCompletedEvent> captor =
                ArgumentCaptor.forClass(AuthChallengeCompletedEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq("securepay.exchange"),
                eq("auth.challenge.completed.notification"),
                captor.capture()
        );

        AuthChallengeCompletedEvent event = captor.getValue();

        assertEquals(transactionId, event.getTransactionId());
        assertEquals("COMPLETED", event.getStatus());
    }

    @Test
    @DisplayName("Should return OTP expired")
    void verify_ShouldReturnOtpExpired() throws Exception {

        UUID transactionId = UUID.randomUUID();

        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setTransactionId(transactionId);
        request.setOtp("123456");

        when(cache.get(transactionId)).thenReturn(null);

        mockMvc.perform(post("/api/v1/notifications/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.status").value("OTP_EXPIRED"));

        verify(cache, never()).delete(any());

        ArgumentCaptor<AuthChallengeCompletedEvent> captor =
                ArgumentCaptor.forClass(AuthChallengeCompletedEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq("securepay.exchange"),
                eq("auth.challenge.completed.notification"),
                captor.capture()
        );

        assertEquals("FAILED", captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should return OTP invalid")
    void verify_ShouldReturnOtpInvalid() throws Exception {

        UUID transactionId = UUID.randomUUID();

        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setTransactionId(transactionId);
        request.setOtp("111111");

        when(cache.get(transactionId)).thenReturn("999999");

        mockMvc.perform(post("/api/v1/notifications/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.status").value("OTP_INVALID"));

        verify(cache, never()).delete(any());

        ArgumentCaptor<AuthChallengeCompletedEvent> captor =
                ArgumentCaptor.forClass(AuthChallengeCompletedEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq("securepay.exchange"),
                eq("auth.challenge.completed.notification"),
                captor.capture()
        );

        assertEquals("FAILED", captor.getValue().getStatus());
    }
}
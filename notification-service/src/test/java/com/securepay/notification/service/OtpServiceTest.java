package com.securepay.notification.service;

import com.securepay.notification.dto.OtpVerificationRequest;
import com.securepay.notification.dto.OtpVerificationResponse;
import com.securepay.notification.event.OtpVerifiedEvent;
import com.securepay.notification.exception.OtpExpiredException;
import com.securepay.notification.exception.OtpInvalidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpCacheService cache;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OtpService otpService;

    @Test
    void verify_ShouldVerifyOtpSuccessfully() {

        UUID transactionId = UUID.randomUUID();

        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setTransactionId(transactionId);
        request.setOtp("123456");

        when(cache.get(transactionId)).thenReturn("123456");

        OtpVerificationResponse response = otpService.verify(request);

        assertNotNull(response);
        assertEquals("OTP_VERIFIED", response.getStatus());

        verify(cache).get(transactionId);
        verify(cache).delete(transactionId);

        ArgumentCaptor<OtpVerifiedEvent> captor =
                ArgumentCaptor.forClass(OtpVerifiedEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq("securepay.exchange"),
                eq("otp.verified"),
                captor.capture()
        );

        OtpVerifiedEvent event = captor.getValue();

        assertNotNull(event);
        assertEquals(transactionId, event.getTransactionId());
    }

    @Test
    void verify_ShouldThrowOtpExpiredException_WhenOtpNotFound() {

        UUID transactionId = UUID.randomUUID();

        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setTransactionId(transactionId);
        request.setOtp("123456");

        when(cache.get(transactionId)).thenReturn(null);

        OtpExpiredException exception = assertThrows(
                OtpExpiredException.class,
                () -> otpService.verify(request)
        );

        assertEquals("OTP expired or not found", exception.getMessage());

        verify(cache).get(transactionId);
        verify(cache, never()).delete(any());
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void verify_ShouldThrowOtpInvalidException_WhenOtpDoesNotMatch() {

        UUID transactionId = UUID.randomUUID();

        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setTransactionId(transactionId);
        request.setOtp("111111");

        when(cache.get(transactionId)).thenReturn("999999");

        OtpInvalidException exception = assertThrows(
                OtpInvalidException.class,
                () -> otpService.verify(request)
        );

        assertEquals("Invalid OTP", exception.getMessage());

        verify(cache).get(transactionId);
        verify(cache, never()).delete(any());
        verifyNoInteractions(rabbitTemplate);
    }
}
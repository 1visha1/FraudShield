package com.fraudshield.notification.service;

import com.fraudshield.notification.event.AuthChallengeCreatedEvent;
import com.fraudshield.notification.event.OTPGeneratedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private OtpGenerator otpGenerator;

    @Mock
    private OtpCacheService otpCacheService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendNotification_ShouldGenerateSaveAndPublishOtp() {

        UUID transactionId = UUID.randomUUID();

        AuthChallengeCreatedEvent event = new AuthChallengeCreatedEvent();
        event.setTransactionId(transactionId);

        when(otpGenerator.generate()).thenReturn("123456");

        notificationService.sendNotification(event);

        // OTP generated
        verify(otpGenerator, times(1)).generate();

        // OTP stored
        verify(otpCacheService, times(1))
                .save(transactionId, "123456");

        // Capture published event
        ArgumentCaptor<OTPGeneratedEvent> captor =
                ArgumentCaptor.forClass(OTPGeneratedEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq("fraudshield.exchange"),
                eq("otp.generated"),
                captor.capture()
        );

        OTPGeneratedEvent published = captor.getValue();

        assertEquals(transactionId, published.getTransactionId());
        assertEquals("123456", published.getOtp());
    }

    @Test
    void sendNotification_ShouldPublishDifferentOtp() {

        UUID transactionId = UUID.randomUUID();

        AuthChallengeCreatedEvent event = new AuthChallengeCreatedEvent();
        event.setTransactionId(transactionId);

        when(otpGenerator.generate()).thenReturn("987654");

        notificationService.sendNotification(event);

        verify(otpGenerator).generate();
        verify(otpCacheService).save(transactionId, "987654");

        verify(rabbitTemplate).convertAndSend(
                eq("fraudshield.exchange"),
                eq("otp.generated"),
                any(OTPGeneratedEvent.class)
        );
    }
}
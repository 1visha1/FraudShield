package com.securepay.notification.service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class EmailService {

    public void send(
            UUID transactionId,
            String otp){

        log.info(
                "EMAIL OTP {} sent for txn {}",
                otp,
                transactionId);
    }
}
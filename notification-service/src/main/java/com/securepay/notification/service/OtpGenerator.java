package com.securepay.notification.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
@Service
public class OtpGenerator {

    public String generate() {

        int otp =
                ThreadLocalRandom
                        .current()
                        .nextInt(
                                100000,
                                999999);

        return String.valueOf(otp);
    }
}
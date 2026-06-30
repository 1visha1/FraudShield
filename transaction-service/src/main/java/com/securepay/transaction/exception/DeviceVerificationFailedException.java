package com.securepay.transaction.exception;

public class DeviceVerificationFailedException extends RuntimeException {

    public DeviceVerificationFailedException(String message) {
        super(message);
    }
}

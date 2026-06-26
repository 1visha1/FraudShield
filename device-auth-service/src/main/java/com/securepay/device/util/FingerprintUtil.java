package com.securepay.device.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class FingerprintUtil {

    public static String generate(
            String deviceId,
            String userAgent,
            String ipAddress,
            String timezone,
            String osVersion) {

        try {

            String raw =
                    deviceId +
                            userAgent +
                            ipAddress +
                            timezone +
                            osVersion;

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            raw.getBytes(
                                    StandardCharsets.UTF_8));

            StringBuilder sb =
                    new StringBuilder();

            for (byte b : hash) {
                sb.append(
                        String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
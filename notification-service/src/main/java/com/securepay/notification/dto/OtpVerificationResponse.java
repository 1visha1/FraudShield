package com.securepay.notification.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpVerificationResponse {

    private String status;
}

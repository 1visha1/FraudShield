package com.fraudshield.transaction.client;

import com.fraudshield.transaction.client.dto.DeviceVerificationRequest;
import com.fraudshield.transaction.client.dto.DeviceVerificationResponse;
import com.fraudshield.transaction.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DeviceAuthService {

    private final WebClient webClient;
    private final ReactiveCircuitBreakerFactory circuitBreakerFactory;

    public DeviceAuthService(WebClient.Builder webClientBuilder, ReactiveCircuitBreakerFactory circuitBreakerFactory) {
        this.webClient = webClientBuilder.baseUrl("http://device-auth-service").build();
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public Mono<ApiResponse<DeviceVerificationResponse>> verifyDevice(DeviceVerificationRequest request) {
        log.info("Sending device verification request for customer: {}", request.getCustomerId());
        return webClient.post()
                .uri("/api/v1/devices/verify")
                .body(Mono.just(request), DeviceVerificationRequest.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<DeviceVerificationResponse>>() {})
                .transform(it -> circuitBreakerFactory.create("device-auth-service").run(it, throwable -> {
                    log.error("Circuit breaker opened for device-auth-service. Falling back.", throwable);
                    // Fallback response
                    return Mono.just(ApiResponse.<DeviceVerificationResponse>builder()
                            .success(false)
                            .message("Device verification service is unavailable. Please try again later.")
                            .errorCode("SERVICE_UNAVAILABLE")
                            .build());
                }));
    }
}
package com.fraudshield.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudshield.gateway.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.LocalDateTime;

@Component
@Order(-2)
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String errorCode;
        String message;

        if (ex instanceof AuthenticationException || ex instanceof AuthenticationCredentialsNotFoundException) {
            status = HttpStatus.UNAUTHORIZED;
            errorCode = "UNAUTHORIZED";
            message = "Authentication is required to access this resource.";
            log.warn("Authentication failure on {}: {}", exchange.getRequest().getPath(), ex.getMessage());
        } else if (ex instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            errorCode = "ACCESS_DENIED";
            message = "You do not have permission to access this resource.";
            log.warn("Access denied on {}: {}", exchange.getRequest().getPath(), ex.getMessage());
        } else if (ex instanceof ResponseStatusException responseStatusException) {
            status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            errorCode = "GATEWAY_" + status.name();
            message = responseStatusException.getReason() != null
                    ? responseStatusException.getReason()
                    : "Gateway error: " + status.getReasonPhrase();
            log.error("Response status exception on {}: {}", exchange.getRequest().getPath(), ex.getMessage());
        } else if (ex.getCause() instanceof ConnectException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorCode = "SERVICE_UNAVAILABLE";
            message = "The downstream service is currently unavailable. Please try again later.";
            log.error("Service unavailable for {}: {}", exchange.getRequest().getPath(), ex.getMessage());
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = "GATEWAY_ERROR";
            message = "An unexpected error occurred in the gateway.";
            log.error("Unexpected gateway error on {}: {}", exchange.getRequest().getPath(), ex.getMessage(), ex);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .path(exchange.getRequest().getPath().value())
                .timestamp(LocalDateTime.now())
                .build();

        try {
            byte[] responseBytes = objectMapper.writeValueAsBytes(response);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(responseBytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Failed to serialize error response", e);
            return Mono.error(e);
        }
    }
}
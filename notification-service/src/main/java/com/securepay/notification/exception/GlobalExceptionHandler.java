package com.fraudshield.notification.exception;

import com.fraudshield.notification.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleOtpExpiredException(
            OtpExpiredException ex, WebRequest request) {
        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("OTP_EXPIRED")
                .path(request.getDescription(false).substring(4))
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OtpInvalidException.class)
    public ResponseEntity<ApiResponse<Object>> handleOtpInvalidException(
            OtpInvalidException ex, WebRequest request) {
        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("OTP_INVALID")
                .path(request.getDescription(false).substring(4))
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message("Validation Failed")
                .data(errors)
                .path(request.getDescription(false).substring(4))
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message("An unexpected error occurred")
                .errorCode("INTERNAL_SERVER_ERROR")
                .path(request.getDescription(false).substring(4))
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

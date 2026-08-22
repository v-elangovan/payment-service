package com.matrix.payment_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(
            PaymentNotFoundException ex,
            HttpServletRequest request) {

        log.error("Payment not found : {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.error("Validation failed");

        Map<String, String> validationErrors = new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        validationErrors.put(
                                error.getField(),
                                error.getDefaultMessage()));

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        response.put("path", request.getRequestURI());
        response.put("validationErrors", validationErrors);

        return ResponseEntity.badRequest()
                .body(response);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleGatewayException(
            RestClientException ex,
            HttpServletRequest request) {

        log.error("Gateway communication failed", ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .error("Bad Gateway")
                .message("Unable to communicate with payment gateway.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected exception", ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
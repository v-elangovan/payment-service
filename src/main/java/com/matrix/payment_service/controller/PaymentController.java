package com.matrix.payment_service.controller;

import com.matrix.payment_service.dto.PaymentDetailsResponse;
import com.matrix.payment_service.dto.PaymentRequest;
import com.matrix.payment_service.dto.PaymentResponse;
import com.matrix.payment_service.dto.PaymentSummaryResponse;
import com.matrix.payment_service.entity.PaymentStatus;
import com.matrix.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request) {

        log.info("Received create payment request");

        PaymentResponse response = paymentService.createPayment(request);

        log.info("Payment created successfully. PaymentId={}",
                response.getPaymentId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailsResponse> getPayment(
            @PathVariable String paymentId) {

        log.info("GET /payments/{}", paymentId);

        PaymentDetailsResponse response =
                paymentService.getPayment(paymentId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PaymentSummaryResponse>> getPayments(
            @RequestParam(required = false) PaymentStatus status) {

        if (status != null) {

            log.info("Fetching payments with status={}", status);

            return ResponseEntity.ok(
                    paymentService.getPaymentsByStatus(status)
            );
        }

        log.info("Fetching all payments");

        return ResponseEntity.ok(
                paymentService.getAllPayments()
        );
    }
}
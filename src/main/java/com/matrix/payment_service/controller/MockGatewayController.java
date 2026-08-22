package com.matrix.payment_service.controller;

import com.matrix.payment_service.dto.GatewayPaymentRequest;
import com.matrix.payment_service.dto.GatewayPaymentResponse;
import com.matrix.payment_service.service.MockGatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mock-gateway")
@RequiredArgsConstructor
@Slf4j
public class MockGatewayController {

    private final MockGatewayService mockGatewayService;

    @PostMapping("/pay")
    public ResponseEntity<GatewayPaymentResponse> processPayment(
            @Valid @RequestBody GatewayPaymentRequest request) {

        log.info("Mock Gateway API called for PaymentId={}",
                request.getPaymentId());

        GatewayPaymentResponse response =
                mockGatewayService.processPayment(request);

        log.info("Mock Gateway response={}", response.getStatus());

        return ResponseEntity.ok(response);
    }
}
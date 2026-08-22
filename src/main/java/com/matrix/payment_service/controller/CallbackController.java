package com.matrix.payment_service.controller;

import com.matrix.payment_service.dto.CallbackRequest;
import com.matrix.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/callbacks")
@RequiredArgsConstructor
@Slf4j
public class CallbackController {

    private final PaymentService paymentService;

    @PostMapping("/payment")
    public ResponseEntity<Void> receiveCallback(
            @Valid @RequestBody CallbackRequest request) {

        log.info("Received callback for PaymentId={}, Status={}",
                request.getPaymentId(),
                request.getStatus());

        paymentService.processCallback(request);

        log.info("Callback processed successfully.");

        return ResponseEntity.ok().build();
    }
}
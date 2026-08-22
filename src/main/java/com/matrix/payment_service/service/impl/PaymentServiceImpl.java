package com.matrix.payment_service.service.impl;

import com.matrix.payment_service.dto.*;
import com.matrix.payment_service.entity.Payment;
import com.matrix.payment_service.entity.PaymentStatus;
import com.matrix.payment_service.exception.PaymentNotFoundException;
import com.matrix.payment_service.repository.PaymentRepository;
import com.matrix.payment_service.service.GatewayClient;
import com.matrix.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final GatewayClient gatewayClient;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {

        log.info("Creating payment. Amount={}, Currency={}",
                request.getAmount(),
                request.getCurrency());

        Payment payment = Payment.builder()
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        log.info("Payment created successfully. PaymentId={}",
                payment.getPaymentId());

        GatewayPaymentRequest gatewayRequest = GatewayPaymentRequest.builder()
                .paymentId(payment.getPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .build();

        try {

            GatewayPaymentResponse gatewayResponse =
                    gatewayClient.pay(gatewayRequest);

            log.info(
                    "Gateway response received. PaymentId={}, Status={}, TransactionId={}",
                    payment.getPaymentId(),
                    gatewayResponse.getStatus(),
                    gatewayResponse.getTransactionId()
            );

            // Re-fetch payment after callback updates it
            String paymentId = payment.getPaymentId();

            payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        } catch (Exception ex) {

            log.error(
                    "Failed to invoke payment gateway for PaymentId={}",
                    payment.getPaymentId(),
                    ex
            );

            // Mark payment as failed
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment gateway communication failed");

            paymentRepository.save(payment);

            throw ex;
        }

        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .status(payment.getStatus())
                .build();
    }

    @Override
    public PaymentDetailsResponse getPayment(String paymentId) {

        log.info("Fetching payment details. PaymentId={}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found. PaymentId={}", paymentId);
                    return new PaymentNotFoundException(paymentId);
                });

        log.info("Payment found. PaymentId={}, Status={}",
                payment.getPaymentId(),
                payment.getStatus());

        return PaymentDetailsResponse.builder()
                .paymentId(payment.getPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .build();
    }

    @Override
    @Transactional
    public void processCallback(CallbackRequest request) {

        log.info(
                "Received callback. PaymentId={}, Status={}",
                request.getPaymentId(),
                request.getStatus()
        );

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> {
                    log.error("Payment not found during callback. PaymentId={}",
                            request.getPaymentId());
                    return new PaymentNotFoundException(request.getPaymentId());
                });

        // Ignore duplicate callback
        if (payment.getStatus() != PaymentStatus.PENDING) {

            log.warn(
                    "Duplicate callback ignored. PaymentId={}, CurrentStatus={}",
                    payment.getPaymentId(),
                    payment.getStatus()
            );

            return;
        }

        payment.setStatus(request.getStatus());
        payment.setTransactionId(request.getTransactionId());
        payment.setFailureReason(request.getFailureReason());

        paymentRepository.save(payment);

        log.info(
                "Payment updated successfully. PaymentId={}, Status={}",
                payment.getPaymentId(),
                payment.getStatus()
        );
    }

    @Override
    public List<PaymentSummaryResponse> getAllPayments() {

        log.info("Fetching all payments");

        return paymentRepository.findAll()
                .stream()
                .map(payment -> PaymentSummaryResponse.builder()
                        .paymentId(payment.getPaymentId())
                        .amount(payment.getAmount())
                        .currency(payment.getCurrency())
                        .status(payment.getStatus())
                        .build())
                .toList();
    }

    @Override
    public List<PaymentSummaryResponse> getPaymentsByStatus(PaymentStatus status) {

        log.info("Fetching payments with status={}", status);

        return paymentRepository.findByStatus(status)
                .stream()
                .map(payment -> PaymentSummaryResponse.builder()
                        .paymentId(payment.getPaymentId())
                        .amount(payment.getAmount())
                        .currency(payment.getCurrency())
                        .status(payment.getStatus())
                        .build())
                .toList();
    }
}
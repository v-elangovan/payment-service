package com.matrix.payment_service.service.impl;

import com.matrix.payment_service.dto.*;
import com.matrix.payment_service.entity.Payment;
import com.matrix.payment_service.entity.PaymentStatus;
import com.matrix.payment_service.exception.PaymentNotFoundException;
import com.matrix.payment_service.repository.PaymentRepository;
import com.matrix.payment_service.service.GatewayClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private GatewayClient gatewayClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void shouldCreatePaymentSuccessfully() {

        PaymentRequest request = PaymentRequest.builder()
                .amount(BigDecimal.valueOf(1000))
                .currency("INR")
                .build();

        Payment payment = Payment.builder()
                .paymentId("PAY123")
                .amount(BigDecimal.valueOf(1000))
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        when(gatewayClient.pay(any(GatewayPaymentRequest.class)))
                .thenReturn(GatewayPaymentResponse.builder()
                        .status(PaymentStatus.SUCCESS)
                        .transactionId("TXN123")
                        .build());

        when(paymentRepository.findById("PAY123"))
                .thenReturn(Optional.of(payment));

        PaymentResponse response =
                paymentService.createPayment(request);

        assertNotNull(response);
        assertEquals("PAY123", response.getPaymentId());

        verify(paymentRepository, atLeastOnce())
                .save(any(Payment.class));

        verify(gatewayClient)
                .pay(any(GatewayPaymentRequest.class));
    }

    @Test
    void shouldMarkPaymentFailedWhenGatewayFails() {

        PaymentRequest request = PaymentRequest.builder()
                .amount(BigDecimal.valueOf(1000))
                .currency("INR")
                .build();

        Payment payment = Payment.builder()
                .paymentId("PAY123")
                .amount(BigDecimal.valueOf(1000))
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        when(gatewayClient.pay(any(GatewayPaymentRequest.class)))
                .thenThrow(new RuntimeException("Gateway unavailable"));

        assertThrows(
                RuntimeException.class,
                () -> paymentService.createPayment(request)
        );

        verify(paymentRepository, atLeast(2))
                .save(any(Payment.class));
    }

    @Test
    void shouldReturnPaymentDetails() {

        Payment payment = Payment.builder()
                .paymentId("PAY123")
                .amount(BigDecimal.valueOf(1000))
                .currency("INR")
                .status(PaymentStatus.SUCCESS)
                .transactionId("TXN123")
                .build();

        when(paymentRepository.findById("PAY123"))
                .thenReturn(Optional.of(payment));

        PaymentDetailsResponse response =
                paymentService.getPayment("PAY123");

        assertEquals("PAY123", response.getPaymentId());
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());

        verify(paymentRepository)
                .findById("PAY123");
    }

    @Test
    void shouldThrowExceptionWhenPaymentNotFound() {

        when(paymentRepository.findById("PAY123"))
                .thenReturn(Optional.empty());

        assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.getPayment("PAY123")
        );

        verify(paymentRepository)
                .findById("PAY123");
    }

    @Test
    void shouldProcessCallbackSuccessfully() {

        CallbackRequest request = CallbackRequest.builder()
                .paymentId("PAY123")
                .status(PaymentStatus.SUCCESS)
                .transactionId("TXN123")
                .build();

        Payment payment = Payment.builder()
                .paymentId("PAY123")
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findById("PAY123"))
                .thenReturn(Optional.of(payment));

        paymentService.processCallback(request);

        assertEquals(
                PaymentStatus.SUCCESS,
                payment.getStatus());

        assertEquals(
                "TXN123",
                payment.getTransactionId());

        verify(paymentRepository)
                .save(payment);
    }

    @Test
    void shouldIgnoreDuplicateCallback() {

        CallbackRequest request = CallbackRequest.builder()
                .paymentId("PAY123")
                .status(PaymentStatus.SUCCESS)
                .transactionId("TXN123")
                .build();

        Payment payment = Payment.builder()
                .paymentId("PAY123")
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentRepository.findById("PAY123"))
                .thenReturn(Optional.of(payment));

        paymentService.processCallback(request);

        verify(paymentRepository, never())
                .save(any());
    }

    @Test
    void shouldReturnAllPayments() {

        Payment payment1 = Payment.builder()
                .paymentId("PAY001")
                .amount(BigDecimal.valueOf(1000))
                .currency("INR")
                .status(PaymentStatus.SUCCESS)
                .build();

        Payment payment2 = Payment.builder()
                .paymentId("PAY002")
                .amount(BigDecimal.valueOf(5000))
                .currency("INR")
                .status(PaymentStatus.FAILED)
                .build();

        when(paymentRepository.findAll())
                .thenReturn(List.of(payment1, payment2));

        List<PaymentSummaryResponse> result =
                paymentService.getAllPayments();

        assertEquals(2, result.size());

        assertEquals(
                "PAY001",
                result.get(0).getPaymentId());

        assertEquals(
                "PAY002",
                result.get(1).getPaymentId());

        verify(paymentRepository)
                .findAll();
    }

    @Test
    void shouldReturnPaymentsByStatus() {

        Payment payment = Payment.builder()
                .paymentId("PAY123")
                .amount(BigDecimal.valueOf(1000))
                .currency("INR")
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentRepository.findByStatus(PaymentStatus.SUCCESS))
                .thenReturn(List.of(payment));

        List<PaymentSummaryResponse> result =
                paymentService.getPaymentsByStatus(
                        PaymentStatus.SUCCESS);

        assertEquals(1, result.size());

        assertEquals(
                PaymentStatus.SUCCESS,
                result.get(0).getStatus());

        verify(paymentRepository)
                .findByStatus(PaymentStatus.SUCCESS);
    }
}
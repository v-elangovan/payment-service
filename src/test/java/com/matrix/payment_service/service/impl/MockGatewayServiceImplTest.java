package com.matrix.payment_service.service.impl;

import com.matrix.payment_service.dto.GatewayPaymentRequest;
import com.matrix.payment_service.dto.GatewayPaymentResponse;
import com.matrix.payment_service.entity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockGatewayServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MockGatewayServiceImpl service;

    @BeforeEach
    void setup() {

        ReflectionTestUtils.setField(
                service,
                "callbackUrl",
                "http://localhost:8080/callbacks/payment");
    }

    @Test
    void shouldReturnSuccessForValidPayment() throws Exception {

        GatewayPaymentRequest request = GatewayPaymentRequest.builder()
                .paymentId("PAY123")
                .amount(BigDecimal.valueOf(2000))
                .currency("INR")
                .build();

        GatewayPaymentResponse response = service.processPayment(request);

        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertNotNull(response.getTransactionId());

        // Wait for async callback thread
        Thread.sleep(1200);

        verify(restTemplate, times(1))
                .postForObject(anyString(), any(), eq(Void.class));
    }

    @Test
    void shouldReturnFailedWhenAmountGreaterThan5000() throws Exception {

        GatewayPaymentRequest request = GatewayPaymentRequest.builder()
                .paymentId("PAY123")
                .amount(BigDecimal.valueOf(10000))
                .currency("INR")
                .build();

        GatewayPaymentResponse response = service.processPayment(request);

        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertEquals("Insufficient Funds", response.getFailureReason());

        Thread.sleep(1200);

        verify(restTemplate, times(1))
                .postForObject(anyString(), any(), eq(Void.class));
    }

    @Test
    void shouldReturnFailedForNonINRCurrency() {

        GatewayPaymentRequest request = GatewayPaymentRequest.builder()
                .paymentId("PAY123")
                .amount(BigDecimal.valueOf(1000))
                .currency("USD")
                .build();

        GatewayPaymentResponse response = service.processPayment(request);

        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertEquals(
                "Only INR currency is supported",
                response.getFailureReason());

        verify(restTemplate, times(1))
                .postForObject(anyString(), any(), eq(Void.class));
    }
}
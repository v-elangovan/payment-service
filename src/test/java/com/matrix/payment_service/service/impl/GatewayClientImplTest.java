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
class GatewayClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GatewayClientImpl gatewayClient;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(
                gatewayClient,
                "gatewayUrl",
                "http://localhost:8080/mock-gateway/pay");
    }

    @Test
    void shouldCallGatewaySuccessfully() {

        GatewayPaymentRequest request = GatewayPaymentRequest.builder()
                .paymentId("PAY123")
                .amount(BigDecimal.valueOf(1000))
                .currency("INR")
                .build();

        GatewayPaymentResponse response = GatewayPaymentResponse.builder()
                .status(PaymentStatus.SUCCESS)
                .transactionId("TXN123")
                .build();

        when(restTemplate.postForObject(
                anyString(),
                any(GatewayPaymentRequest.class),
                eq(GatewayPaymentResponse.class)))
                .thenReturn(response);

        GatewayPaymentResponse result = gatewayClient.pay(request);

        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        assertEquals("TXN123", result.getTransactionId());

        verify(restTemplate, times(1))
                .postForObject(
                        anyString(),
                        any(GatewayPaymentRequest.class),
                        eq(GatewayPaymentResponse.class));
    }

    @Test
    void shouldThrowExceptionWhenGatewayReturnsNull() {

        GatewayPaymentRequest request = GatewayPaymentRequest.builder()
                .paymentId("PAY123")
                .amount(BigDecimal.valueOf(1000))
                .currency("INR")
                .build();

        when(restTemplate.postForObject(
                anyString(),
                any(GatewayPaymentRequest.class),
                eq(GatewayPaymentResponse.class)))
                .thenReturn(null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gatewayClient.pay(request)
        );

        assertEquals(
                "No response received from gateway.",
                exception.getMessage()
        );

        verify(restTemplate, times(1))
                .postForObject(
                        anyString(),
                        any(GatewayPaymentRequest.class),
                        eq(GatewayPaymentResponse.class));
    }
}
package com.matrix.payment_service.service.impl;

import com.matrix.payment_service.dto.GatewayPaymentRequest;
import com.matrix.payment_service.dto.GatewayPaymentResponse;
import com.matrix.payment_service.service.GatewayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GatewayClientImpl implements GatewayClient {
    private final RestTemplate restTemplate;
    @Value("${gateway.url}")
    private String gatewayUrl;

    @Override
    public GatewayPaymentResponse pay(GatewayPaymentRequest request) {
        log.info("Calling Mock Gateway for PaymentId={}", request.getPaymentId());
        GatewayPaymentResponse response = restTemplate.postForObject(gatewayUrl, request, GatewayPaymentResponse.class);
        if (response == null) {
            throw new IllegalStateException("No response received from gateway.");
        }
        log.info("Gateway Response received. Status={}", response.getStatus());
        return response;
    }
}
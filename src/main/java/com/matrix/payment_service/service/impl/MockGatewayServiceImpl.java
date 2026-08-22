package com.matrix.payment_service.service.impl;

import com.matrix.payment_service.dto.CallbackRequest;
import com.matrix.payment_service.dto.GatewayPaymentRequest;
import com.matrix.payment_service.dto.GatewayPaymentResponse;
import com.matrix.payment_service.entity.PaymentStatus;
import com.matrix.payment_service.service.MockGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockGatewayServiceImpl implements MockGatewayService {

    private final RestTemplate restTemplate;

    @Value("${callback.url}")
    private String callbackUrl;

    @Override
    public GatewayPaymentResponse processPayment(GatewayPaymentRequest request) {

        log.info("Mock Gateway received payment request. PaymentId={}",
                request.getPaymentId());

        GatewayPaymentResponse response = new GatewayPaymentResponse();
        CallbackRequest callback = new CallbackRequest();

        callback.setPaymentId(request.getPaymentId());

        if (!"INR".equalsIgnoreCase(request.getCurrency())) {

            response.setStatus(PaymentStatus.FAILED);
            response.setFailureReason("Only INR currency is supported");

            callback.setStatus(PaymentStatus.FAILED);
            callback.setFailureReason("Only INR currency is supported");

            restTemplate.postForObject(
                    callbackUrl,
                    callback,
                    Void.class
            );

            return response;
        }

        if (request.getAmount().compareTo(BigDecimal.valueOf(5000)) <= 0) {

            String transactionId = UUID.randomUUID().toString();

            response.setStatus(PaymentStatus.SUCCESS);
            response.setTransactionId(transactionId);

            callback.setStatus(PaymentStatus.SUCCESS);
            callback.setTransactionId(transactionId);

            log.info("Payment successful. PaymentId={}, TransactionId={}",
                    request.getPaymentId(),
                    transactionId);

        } else {

            response.setStatus(PaymentStatus.FAILED);
            response.setFailureReason("Insufficient Funds");

            callback.setStatus(PaymentStatus.FAILED);
            callback.setFailureReason("Insufficient Funds");

            log.warn("Payment failed. PaymentId={}, Reason=Insufficient Funds",
                    request.getPaymentId());
        }

        log.info("Sending callback for PaymentId={}",
                request.getPaymentId());

        new Thread(() -> {
            try {
                Thread.sleep(1000); // simulate gateway delay

                restTemplate.postForObject(
                        callbackUrl,
                        callback,
                        Void.class
                );

            } catch (Exception e) {
                log.error("Callback failed", e);
            }
        }).start();

        log.info("Callback sent successfully.");

        return response;
    }
}
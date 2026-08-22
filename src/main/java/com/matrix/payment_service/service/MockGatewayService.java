package com.matrix.payment_service.service;

import com.matrix.payment_service.dto.GatewayPaymentRequest;
import com.matrix.payment_service.dto.GatewayPaymentResponse;

public interface MockGatewayService {

    GatewayPaymentResponse processPayment(GatewayPaymentRequest request);

}
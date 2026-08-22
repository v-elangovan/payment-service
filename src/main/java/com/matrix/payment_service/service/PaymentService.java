package com.matrix.payment_service.service;

import com.matrix.payment_service.dto.*;
import com.matrix.payment_service.entity.PaymentStatus;

import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest request);

    PaymentDetailsResponse getPayment(String paymentId);

    void processCallback(CallbackRequest request);

    List<PaymentSummaryResponse> getAllPayments();

    List<PaymentSummaryResponse> getPaymentsByStatus(PaymentStatus status);
}

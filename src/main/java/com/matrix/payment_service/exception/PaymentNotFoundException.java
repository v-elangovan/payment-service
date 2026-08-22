package com.matrix.payment_service.exception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String paymentId) {
        super("Payment not found with id : " + paymentId);
    }
}
package com.matrix.payment_service.dto;

import com.matrix.payment_service.entity.PaymentStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private String paymentId;

    private PaymentStatus status;
}
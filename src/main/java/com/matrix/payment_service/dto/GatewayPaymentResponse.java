package com.matrix.payment_service.dto;

import com.matrix.payment_service.entity.PaymentStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayPaymentResponse {

    private PaymentStatus status;

    private String transactionId;

    private String failureReason;
}

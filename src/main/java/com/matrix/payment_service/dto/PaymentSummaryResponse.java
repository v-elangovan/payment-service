package com.matrix.payment_service.dto;

import com.matrix.payment_service.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryResponse {

    private String paymentId;

    private BigDecimal amount;

    private String currency;

    private PaymentStatus status;
}
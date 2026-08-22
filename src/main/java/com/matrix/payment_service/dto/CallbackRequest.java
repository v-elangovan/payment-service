package com.matrix.payment_service.dto;

import com.matrix.payment_service.entity.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallbackRequest {

    @NotBlank(message = "Payment Id is required")
    private String paymentId;

    @NotNull(message = "Status is required")
    private PaymentStatus status;

    private String transactionId;

    private String failureReason;
}

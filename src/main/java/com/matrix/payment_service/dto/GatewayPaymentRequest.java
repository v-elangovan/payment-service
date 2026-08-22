package com.matrix.payment_service.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayPaymentRequest {

    @NotBlank(message = "Payment Id is required")
    private String paymentId;

    @NotNull(message="Amount is required")
    @Positive
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(
            regexp = "INR",
            message = "Only INR currency is supported"
    )
    private String currency;
}

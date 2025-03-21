package com.rafaelgalvezg.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaymentRequestDto(
        @NotNull(message = "Order ID cannot be null")
        Long orderId,
        @NotNull(message = "Total cannot be null")
        @Min(value = 0, message = "Total must be greater than zero")
        Double total
) {}
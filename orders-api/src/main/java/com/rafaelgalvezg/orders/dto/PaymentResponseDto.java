package com.rafaelgalvezg.orders.dto;

import java.time.LocalDateTime;

public record PaymentResponseDto(
        Long id,
        Long orderId,
        Double total,
        String status,
        LocalDateTime timestamp
) {}
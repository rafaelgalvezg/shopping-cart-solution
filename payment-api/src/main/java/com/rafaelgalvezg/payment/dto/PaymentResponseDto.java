package com.rafaelgalvezg.payment.dto;

import com.rafaelgalvezg.payment.entity.Payment;

import java.time.LocalDateTime;

public record PaymentResponseDto(
        Long id,
        Long orderId,
        Double total,
        String status,
        LocalDateTime timestamp
) {
    public static PaymentResponseDto fromEntity(Payment payment) {
        return new PaymentResponseDto(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt()
        );
    }
}
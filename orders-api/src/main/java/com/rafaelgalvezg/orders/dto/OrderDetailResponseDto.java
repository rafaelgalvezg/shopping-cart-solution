package com.rafaelgalvezg.orders.dto;

import com.rafaelgalvezg.orders.entity.OrderDetail;

public record OrderDetailResponseDto(
        Long id,
        Long productId,
        Integer quantity,
        Double unitPrice
) {
    public static OrderDetailResponseDto fromEntity(OrderDetail detail) {
        return new OrderDetailResponseDto(
                detail.getId(),
                detail.getProductId(),
                detail.getQuantity(),
                detail.getUnitPrice()
        );
    }
}
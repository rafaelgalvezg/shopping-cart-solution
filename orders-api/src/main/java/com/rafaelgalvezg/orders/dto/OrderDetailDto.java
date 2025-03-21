package com.rafaelgalvezg.orders.dto;

import com.rafaelgalvezg.orders.entity.OrderDetail;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderDetailDto(
        @NotNull(message = "Product ID cannot be null")
        Long productId,

        @NotNull(message = "Quantity cannot be null")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {
    public OrderDetail toEntity() {
        OrderDetail detail = new OrderDetail();
        detail.setProductId(productId);
        detail.setQuantity(quantity);
        return detail;
    }
}
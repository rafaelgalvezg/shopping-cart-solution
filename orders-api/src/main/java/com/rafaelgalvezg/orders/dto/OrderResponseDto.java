package com.rafaelgalvezg.orders.dto;

import com.rafaelgalvezg.orders.entity.Order;
import com.rafaelgalvezg.orders.entity.OrderStatus;

import java.util.List;

public record OrderResponseDto(
        Long id,
        Long clientId,
        String clientName,
        String clientEmail,
        List<OrderDetailResponseDto> details,
        Double total,
        OrderStatus status
) {
    public static OrderResponseDto fromEntity(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getClient().getId(),
                order.getClient().getName(),
                order.getClient().getEmail(),
                order.getDetails().stream().map(OrderDetailResponseDto::fromEntity).toList(),
                order.getTotal(),
                order.getStatus()
        );
    }
}
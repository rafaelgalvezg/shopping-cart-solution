package com.rafaelgalvezg.orders.service;

import com.rafaelgalvezg.orders.dto.OrderRequestDto;
import com.rafaelgalvezg.orders.dto.OrderResponseDto;

import java.util.List;

public interface OrderService {
    OrderResponseDto createOrder(OrderRequestDto request);
    OrderResponseDto processPayment(Long orderId);
    List<OrderResponseDto> getAllOrders();
    OrderResponseDto getOrderById(Long id);
    OrderResponseDto updateOrder(Long id, OrderRequestDto request);
    void deleteOrder(Long id);
}
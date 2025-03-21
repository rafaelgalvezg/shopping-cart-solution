package com.rafaelgalvezg.orders;

import com.rafaelgalvezg.orders.dto.OrderDetailDto;
import com.rafaelgalvezg.orders.dto.OrderRequestDto;
import com.rafaelgalvezg.orders.dto.PaymentResponseDto;
import com.rafaelgalvezg.orders.dto.ProductDto;
import com.rafaelgalvezg.orders.entity.Order;
import com.rafaelgalvezg.orders.entity.OrderDetail;
import com.rafaelgalvezg.orders.entity.OrderStatus;

import java.util.ArrayList;
import java.util.List;

public class OrderTestFactory {

    public static OrderRequestDto createOrderRequestDto() {
        return new OrderRequestDto(
                "Rafael Galvez",
                "rafael@example.com",
                List.of(new OrderDetailDto(1L, 2))
        );
    }

    public static Order createOrder(OrderStatus status) {
        Order order = new Order();
        order.setId(1L);
        order.setClient(ClientTestFactory.createClient());
        order.setStatus(status);
        order.setTotal(0.0);
        OrderDetail detail = new OrderDetail();
        detail.setProductId(1L);
        detail.setQuantity(2);
        detail.setUnitPrice(0.0);
        detail.setOrder(order);
        order.setDetails(new ArrayList<>(List.of(detail)));
        return order;
    }

    public static ProductDto createProductDto() {
        return new ProductDto(1L, 10.0);
    }

    public static PaymentResponseDto createPaymentResponseDto(String status) {
        return new PaymentResponseDto(1L, 1L, 10.0, status, null);
    }
}
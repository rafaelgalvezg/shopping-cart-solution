package com.rafaelgalvezg.orders.dto;

import com.rafaelgalvezg.orders.entity.Client;
import com.rafaelgalvezg.orders.entity.Order;
import com.rafaelgalvezg.orders.entity.OrderDetail;
import com.rafaelgalvezg.orders.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public record OrderRequestDto(
        @NotBlank(message = "Client name cannot be blank")
        String clientName,

        @NotBlank(message = "Client email cannot be blank")
        @Email(message = "Client email must be valid")
        String clientEmail,

        @NotEmpty(message = "Details cannot be empty")
        @Size(min = 1, message = "At least one detail is required")
        List<@Valid OrderDetailDto> details
) {
    public Order toEntity(Client client) {
        Order order = new Order();
        order.setClient(client);
        order.setStatus(OrderStatus.CREATED);
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (OrderDetailDto detailDto : details) {
            OrderDetail detail = detailDto.toEntity();
            detail.setOrder(order);
            orderDetails.add(detail);
        }
        order.setDetails(orderDetails);
        return order;
    }
}
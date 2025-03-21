package com.rafaelgalvezg.orders.dto;

public record PaymentRequestDto(Long orderId, Double total) {}
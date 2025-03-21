package com.rafaelgalvezg.payment;

import com.rafaelgalvezg.payment.dto.PaymentRequestDto;
import com.rafaelgalvezg.payment.dto.PaymentResponseDto;
import com.rafaelgalvezg.payment.entity.Payment;

public class PaymentTestFactory {

    public static PaymentRequestDto createPaymentRequestDto() {
        return new PaymentRequestDto(1L, 20.0);
    }

    public static Payment createPayment(String status) {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(1L);
        payment.setAmount(20.0);
        payment.setStatus(status);
        return payment;
    }

    public static PaymentResponseDto createPaymentResponseDto(String status) {
        return new PaymentResponseDto(1L, 1L, 20.0, status, null);
    }
}
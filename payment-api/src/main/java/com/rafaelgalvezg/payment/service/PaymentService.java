package com.rafaelgalvezg.payment.service;

import com.rafaelgalvezg.payment.dto.PaymentRequestDto;
import com.rafaelgalvezg.payment.dto.PaymentResponseDto;

import java.util.List;

public interface PaymentService {
    PaymentResponseDto processPayment(PaymentRequestDto request);
    List<PaymentResponseDto> getAllPayments();
}
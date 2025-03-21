package com.rafaelgalvezg.payment.service.impl;

import com.rafaelgalvezg.payment.dto.PaymentRequestDto;
import com.rafaelgalvezg.payment.dto.PaymentResponseDto;
import com.rafaelgalvezg.payment.entity.Payment;
import com.rafaelgalvezg.payment.exception.PaymentProcessingException;
import com.rafaelgalvezg.payment.repository.PaymentRepository;
import com.rafaelgalvezg.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final Random random = new Random();

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        log.info("Processing payment for order ID: {} with total: {}", request.orderId(), request.total());

        if (request.total() <= 0) {
            log.error("Invalid total for order ID: {}: {}", request.orderId(), request.total());
            throw new PaymentProcessingException("Total must be greater than zero");
        }

        Payment payment = createPayment(request);
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment saved with ID: {} for order ID: {}", savedPayment.getId(), savedPayment.getOrderId());

        return PaymentResponseDto.fromEntity(savedPayment);
    }

    @Override
    public List<PaymentResponseDto> getAllPayments() {
        log.debug("Fetching all payments from database");
        List<Payment> payments = paymentRepository.findAll();
        List<PaymentResponseDto> paymentDtos = payments.stream()
                .map(PaymentResponseDto::fromEntity).toList();
        log.info("Retrieved {} payments", paymentDtos.size());
        return paymentDtos;
    }

    private Payment createPayment(PaymentRequestDto request) {
        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setAmount(request.total());
        boolean paymentSuccess = random.nextDouble() < 0.8;
        payment.setStatus(paymentSuccess ? "SUCCESS" : "FAILED");
        return payment;
    }
}
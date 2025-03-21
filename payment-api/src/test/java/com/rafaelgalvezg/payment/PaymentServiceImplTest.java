package com.rafaelgalvezg.payment;

import com.rafaelgalvezg.payment.dto.PaymentRequestDto;
import com.rafaelgalvezg.payment.dto.PaymentResponseDto;
import com.rafaelgalvezg.payment.entity.Payment;
import com.rafaelgalvezg.payment.exception.PaymentProcessingException;
import com.rafaelgalvezg.payment.repository.PaymentRepository;
import com.rafaelgalvezg.payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private Random random;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "random", random);
    }

    @Test
    @DisplayName("Should process payment successfully and return SUCCESS status")
    void processPayment_success() {
        // Arrange
        PaymentRequestDto request = PaymentTestFactory.createPaymentRequestDto();
        Payment payment = PaymentTestFactory.createPayment("SUCCESS");
        when(random.nextDouble()).thenReturn(0.7); // < 0.8, éxito
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        PaymentResponseDto result = paymentService.processPayment(request);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.id()).isEqualTo(1L);
                    assertThat(r.orderId()).isEqualTo(1L);
                    assertThat(r.total()).isEqualTo(20.0);
                    assertThat(r.status()).isEqualTo("SUCCESS");
                });
        verify(random, times(1)).nextDouble();
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should process payment and return FAILED status when payment fails")
    void processPayment_failure() {
        // Arrange
        PaymentRequestDto request = PaymentTestFactory.createPaymentRequestDto();
        Payment payment = PaymentTestFactory.createPayment("FAILED");
        when(random.nextDouble()).thenReturn(0.9); // >= 0.8, fallo
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        PaymentResponseDto result = paymentService.processPayment(request);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.id()).isEqualTo(1L);
                    assertThat(r.orderId()).isEqualTo(1L);
                    assertThat(r.total()).isEqualTo(20.0);
                    assertThat(r.status()).isEqualTo("FAILED");
                });
        verify(random, times(1)).nextDouble();
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw PaymentProcessingException when total is zero or negative")
    void processPayment_invalidTotal_throwsPaymentProcessingException() {
        // Arrange
        PaymentRequestDto request = new PaymentRequestDto(1L, 0.0); // Total inválido

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessage("Total must be greater than zero");
        verify(random, never()).nextDouble();
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should return list of all payments")
    void getAllPayments_success() {
        // Arrange
        Payment payment = PaymentTestFactory.createPayment("SUCCESS");
        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        // Act
        List<PaymentResponseDto> result = paymentService.getAllPayments();

        // Assert
        assertThat(result)
                .hasSize(1)
                .satisfiesExactly(r -> {
                    assertThat(r.id()).isEqualTo(1L);
                    assertThat(r.orderId()).isEqualTo(1L);
                    assertThat(r.total()).isEqualTo(20.0);
                    assertThat(r.status()).isEqualTo("SUCCESS");
                });
        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no payments exist")
    void getAllPayments_empty() {
        // Arrange
        when(paymentRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<PaymentResponseDto> result = paymentService.getAllPayments();

        // Assert
        assertThat(result).isEmpty();
        verify(paymentRepository, times(1)).findAll();
    }
}
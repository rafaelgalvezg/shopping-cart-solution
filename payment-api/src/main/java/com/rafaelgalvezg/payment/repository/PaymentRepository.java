package com.rafaelgalvezg.payment.repository;

import com.rafaelgalvezg.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
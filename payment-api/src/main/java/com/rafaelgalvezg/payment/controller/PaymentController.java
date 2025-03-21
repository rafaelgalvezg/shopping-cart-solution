package com.rafaelgalvezg.payment.controller;

import com.rafaelgalvezg.payment.dto.PaymentRequestDto;
import com.rafaelgalvezg.payment.dto.PaymentResponseDto;
import com.rafaelgalvezg.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "API for managing payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Process a payment", description = "Processes a payment for an order with the provided order ID and total amount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or total amount less than or equal to zero")
    })
    public ResponseEntity<PaymentResponseDto> processPayment(@Valid @RequestBody PaymentRequestDto request) {
        log.info("Received payment request for order ID: {} with total: {}", request.orderId(), request.total());
        PaymentResponseDto response = paymentService.processPayment(request);
        log.info("Payment processed successfully with ID: {}", response.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all payments", description = "Retrieves a list of all processed payments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of payments retrieved successfully")
    })
    public ResponseEntity<List<PaymentResponseDto>> getAllPayments() {
        log.info("Received request to retrieve all payments");
        List<PaymentResponseDto> payments = paymentService.getAllPayments();
        log.info("Retrieved {} payments", payments.size());
        return ResponseEntity.ok(payments);
    }
}
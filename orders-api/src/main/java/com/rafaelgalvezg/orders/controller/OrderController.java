package com.rafaelgalvezg.orders.controller;

import com.rafaelgalvezg.orders.dto.OrderRequestDto;
import com.rafaelgalvezg.orders.dto.OrderResponseDto;
import com.rafaelgalvezg.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "API for managing orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order with the provided client and order details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto request) {
        log.info("Received request to create order for client: {}", request.clientName());
        OrderResponseDto response = orderService.createOrder(request);
        log.info("Order created successfully with ID: {}", response.id());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/pay")
    @Operation(summary = "Process payment for an order", description = "Processes the payment for an existing order identified by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Order cannot be paid due to its current state")
    })
    public ResponseEntity<OrderResponseDto> processPayment(
            @Parameter(description = "ID of the order to process payment for", required = true)
            @PathVariable Long orderId) {
        log.info("Received request to process payment for order ID: {}", orderId);
        OrderResponseDto response = orderService.processPayment(orderId);
        log.info("Payment processed for order ID: {}, status: {}", response.id(), response.status());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieves a list of all orders")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of orders retrieved successfully")
    })
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        log.info("Received request to retrieve all orders");
        List<OrderResponseDto> orders = orderService.getAllOrders();
        log.info("Retrieved {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get an order by ID", description = "Retrieves the details of a specific order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponseDto> getOrderById(
            @Parameter(description = "ID of the order to retrieve", required = true)
            @PathVariable Long orderId) {
        log.info("Received request to retrieve order with ID: {}", orderId);
        OrderResponseDto response = orderService.getOrderById(orderId);
        log.info("Order retrieved successfully with ID: {}", response.id());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}")
    @Operation(summary = "Update an order", description = "Updates the details of an existing order identified by its ID, only if it is in CREATED state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Order cannot be updated due to its current state")
    })
    public ResponseEntity<OrderResponseDto> updateOrder(
            @Parameter(description = "ID of the order to update", required = true)
            @PathVariable Long orderId,
            @Valid @RequestBody OrderRequestDto request) {
        log.info("Received request to update order details with ID: {}", orderId);
        OrderResponseDto response = orderService.updateOrder(orderId, request);
        log.info("Order details updated successfully with ID: {}", response.id());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Delete an order", description = "Deletes an existing order identified by its ID, only if it is in CREATED state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Order cannot be deleted due to its current state")
    })
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "ID of the order to delete", required = true)
            @PathVariable Long orderId) {
        log.info("Received request to delete order with ID: {}", orderId);
        orderService.deleteOrder(orderId);
        log.info("Order deleted successfully with ID: {}", orderId);
        return ResponseEntity.noContent().build();
    }
}
package com.rafaelgalvezg.orders.service.impl;

import com.rafaelgalvezg.orders.dto.*;
import com.rafaelgalvezg.orders.entity.Client;
import com.rafaelgalvezg.orders.entity.Order;
import com.rafaelgalvezg.orders.entity.OrderDetail;
import com.rafaelgalvezg.orders.entity.OrderStatus;
import com.rafaelgalvezg.orders.exception.ExternalServiceException;
import com.rafaelgalvezg.orders.exception.OrderStateException;
import com.rafaelgalvezg.orders.exception.ResourceNotFoundException;
import com.rafaelgalvezg.orders.repository.OrderRepository;
import com.rafaelgalvezg.orders.service.ClientService;
import com.rafaelgalvezg.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final String MESSAGE_ORDER_NOT_FOUND = "Order not found with ID: ";
    private final OrderRepository orderRepository;
    private final ClientService clientService;
    private final RestTemplate restTemplate;

    @Value("${external-services.products-api}")
    private String productsApiUrl;

    @Value("${external-services.payment-api}")
    private String paymentApiUrl;

    private static final Set<OrderStatus> BLOCKING_STATES = EnumSet.of(OrderStatus.CREATED, OrderStatus.PAYMENT_FAILED);
    private static final Set<OrderStatus> PAYABLE_STATES = EnumSet.of(OrderStatus.CREATED, OrderStatus.PAYMENT_FAILED);

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto request) {
        log.info("Creating order for client: {}", request.clientName());

        ClientDto clientDto = new ClientDto(null, request.clientName(), request.clientEmail());
        Client client = clientService.createClient(clientDto);
        validateClientCanCreateOrder(client);

        Order order = request.toEntity(client);
        calculateOrderTotal(order);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());
        return OrderResponseDto.fromEntity(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDto processPayment(Long orderId) {
        log.info("Processing payment for order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_ORDER_NOT_FOUND + orderId));
        validateOrderCanBePaid(order);

        PaymentRequestDto paymentRequest = new PaymentRequestDto(orderId, order.getTotal());
        PaymentResponseDto paymentResponse = restTemplate.postForObject(paymentApiUrl, paymentRequest, PaymentResponseDto.class);
        if (paymentResponse == null) {
            log.error("Payment failed for order ID: {}", orderId);
            throw new IllegalStateException("Payment failed: No response");
        }

        order.setStatus("SUCCESS".equals(paymentResponse.status()) ? OrderStatus.PAID : OrderStatus.PAYMENT_FAILED);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order updated with status: {}, ID: {}", updatedOrder.getStatus(), updatedOrder.getId());
        return OrderResponseDto.fromEntity(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        log.debug("Fetching all orders from database");
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(OrderResponseDto::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        log.debug("Fetching order with ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_ORDER_NOT_FOUND + id));
        return OrderResponseDto.fromEntity(order);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrder(Long id, OrderRequestDto request) {
        log.debug("Updating order details with ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_ORDER_NOT_FOUND + id));

        if (order.getStatus() != OrderStatus.CREATED) {
            log.error("Cannot update order ID: {}. It must be in CREATED state, current status: {}", id, order.getStatus());
            throw new OrderStateException("Order can only be updated if it is in CREATED state");
        }

        order.getDetails().clear();
        List<OrderDetail> newDetails = request.details().stream()
                .map(detailDto -> {
                    OrderDetail detail = detailDto.toEntity();
                    detail.setOrder(order);
                    return detail;
                }).toList();
        order.getDetails().addAll(newDetails);

        calculateOrderTotal(order);

        Order updatedOrder = orderRepository.save(order);
        log.info("Order details updated successfully with ID: {}", updatedOrder.getId());
        return OrderResponseDto.fromEntity(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        log.debug("Deleting order with ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_ORDER_NOT_FOUND + id));

        if (order.getStatus() != OrderStatus.CREATED) {
            log.error("Cannot delete order ID: {}. It must be in CREATED state, current status: {}", id, order.getStatus());
            throw new OrderStateException("Order can only be deleted if it is in CREATED state");
        }

        orderRepository.delete(order);
        log.info("Order deleted with ID: {}", id);
    }

    private void validateClientCanCreateOrder(Client client) {
        if (orderRepository.existsByClientAndStatusIn(client, BLOCKING_STATES.stream().toList())) {
            log.error("Client {} has an order in a blocking state: {}", client.getEmail(), BLOCKING_STATES);
            throw new OrderStateException("Cannot create a new order. Client has an existing order in CREATED or PAYMENT_FAILED state.");
        }
    }

    private void validateOrderCanBePaid(Order order) {
        if (!PAYABLE_STATES.contains(order.getStatus())) {
            log.error("Order {} cannot be paid, current status: {}", order.getId(), order.getStatus());
            throw new OrderStateException("Order cannot be paid. It must be in CREATED or PAYMENT_FAILED state.");
        }
    }

    private void calculateOrderTotal(Order order) {
        double total = 0;
        for (OrderDetail detail : order.getDetails()) {
            log.debug("Fetching product ID: {}", detail.getProductId());
            try {
                ProductDto product = restTemplate.getForObject(
                        productsApiUrl + "/" + detail.getProductId(), ProductDto.class);
                if (product == null) {
                    log.error("Product not found: {}", detail.getProductId());
                    throw new ResourceNotFoundException("Product not found with ID: " + detail.getProductId());
                }
                detail.setUnitPrice(product.price());
                total += product.price() * detail.getQuantity();
            } catch (HttpClientErrorException.NotFound ex) {
                log.error("Product ID: {} not found in products API", detail.getProductId());
                throw new ResourceNotFoundException("Product not found with ID: " + detail.getProductId());
            } catch (HttpClientErrorException ex) {
                log.error("Client error while fetching product ID: {} from products API: {}", detail.getProductId(), ex.getMessage());
                throw new ExternalServiceException("Invalid request to products API: " + ex.getMessage(), ex);
            } catch (HttpServerErrorException ex) {
                log.error("Server error while fetching product ID: {} from products API: {}", detail.getProductId(), ex.getMessage());
                throw new ExternalServiceException("Products API server error: " + ex.getMessage(), ex);
            }
        }
        order.setTotal(total);
    }
}
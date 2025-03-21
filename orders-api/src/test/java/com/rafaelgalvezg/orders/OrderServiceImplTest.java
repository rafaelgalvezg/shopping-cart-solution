package com.rafaelgalvezg.orders;

import com.rafaelgalvezg.orders.dto.OrderRequestDto;
import com.rafaelgalvezg.orders.dto.OrderResponseDto;
import com.rafaelgalvezg.orders.dto.PaymentResponseDto;
import com.rafaelgalvezg.orders.dto.ProductDto;
import com.rafaelgalvezg.orders.entity.Client;
import com.rafaelgalvezg.orders.entity.Order;
import com.rafaelgalvezg.orders.entity.OrderStatus;
import com.rafaelgalvezg.orders.exception.OrderStateException;
import com.rafaelgalvezg.orders.exception.ResourceNotFoundException;
import com.rafaelgalvezg.orders.repository.OrderRepository;
import com.rafaelgalvezg.orders.service.ClientService;
import com.rafaelgalvezg.orders.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientService clientService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "productsApiUrl", "http://localhost:8081/api/products");
        ReflectionTestUtils.setField(orderService, "paymentApiUrl", "http://localhost:8083/api/payments");
    }

    @Test
    @DisplayName("Should create order successfully when client and product are valid")
    void createOrder_success() {
        // Arrange
        OrderRequestDto request = OrderTestFactory.createOrderRequestDto();
        Client client = ClientTestFactory.createClient();
        when(clientService.createClient(any())).thenReturn(client);
        when(orderRepository.existsByClientAndStatusIn(any(), any())).thenReturn(false);
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class)))
                .thenReturn(OrderTestFactory.createProductDto());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponseDto result = orderService.createOrder(request);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.clientName()).isEqualTo("Rafael Galvez");
                    assertThat(r.total()).isEqualTo(20.0); // 10.0 * 2
                    assertThat(r.status()).isEqualTo(OrderStatus.CREATED);
                });
        verify(clientService, times(1)).createClient(any());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(ProductDto.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw OrderStateException when client has blocking order")
    void createOrder_blockingState_throwsOrderStateException() {
        // Arrange
        OrderRequestDto request = OrderTestFactory.createOrderRequestDto();
        Client client = ClientTestFactory.createClient();
        when(clientService.createClient(any())).thenReturn(client);
        when(orderRepository.existsByClientAndStatusIn(any(), any())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(OrderStateException.class)
                .hasMessage("Cannot create a new order. Client has an existing order in CREATED or PAYMENT_FAILED state.");
        verify(clientService, times(1)).createClient(any());
        verify(restTemplate, never()).getForObject(anyString(), eq(ProductDto.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product is not found")
    void createOrder_productNotFound_throwsResourceNotFoundException() {
        // Arrange
        OrderRequestDto request = OrderTestFactory.createOrderRequestDto();
        Client client = ClientTestFactory.createClient();
        when(clientService.createClient(any())).thenReturn(client);
        when(orderRepository.existsByClientAndStatusIn(any(), any())).thenReturn(false);
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with ID: 1");
        verify(restTemplate, times(1)).getForObject(anyString(), eq(ProductDto.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should update order successfully when order is in CREATED state")
    void updateOrder_success() {
        // Arrange
        Order existingOrder = OrderTestFactory.createOrder(OrderStatus.CREATED);
        OrderRequestDto request = OrderTestFactory.createOrderRequestDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class)))
                .thenReturn(OrderTestFactory.createProductDto());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponseDto result = orderService.updateOrder(1L, request);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.id()).isEqualTo(1L);
                    assertThat(r.total()).isEqualTo(20.0); // 10.0 * 2
                    assertThat(r.status()).isEqualTo(OrderStatus.CREATED);
                });
        verify(orderRepository, times(1)).findById(1L);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(ProductDto.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw OrderStateException when order is not in CREATED state")
    void updateOrder_invalidState_throwsOrderStateException() {
        // Arrange
        Order existingOrder = OrderTestFactory.createOrder(OrderStatus.PAID);
        OrderRequestDto request = OrderTestFactory.createOrderRequestDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrder(1L, request))
                .isInstanceOf(OrderStateException.class)
                .hasMessage("Order can only be updated if it is in CREATED state");
        verify(orderRepository, times(1)).findById(1L);
        verify(restTemplate, never()).getForObject(anyString(), eq(ProductDto.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order does not exist")
    void updateOrder_orderNotFound_throwsResourceNotFoundException() {
        // Arrange
        OrderRequestDto request = OrderTestFactory.createOrderRequestDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrder(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found with ID: 1");
        verify(orderRepository, times(1)).findById(1L);
        verify(restTemplate, never()).getForObject(anyString(), eq(ProductDto.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    // --- processPayment ---
    @Test
    @DisplayName("Should process payment successfully and set PAID status")
    void processPayment_success() {
        // Arrange
        Order order = OrderTestFactory.createOrder(OrderStatus.CREATED);
        order.setTotal(20.0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponseDto.class)))
                .thenReturn(OrderTestFactory.createPaymentResponseDto("SUCCESS"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponseDto result = orderService.processPayment(1L);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.id()).isEqualTo(1L);
                    assertThat(r.status()).isEqualTo(OrderStatus.PAID);
                });
        verify(orderRepository, times(1)).findById(1L);
        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(PaymentResponseDto.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should set PAYMENT_FAILED status when payment fails")
    void processPayment_failure() {
        // Arrange
        Order order = OrderTestFactory.createOrder(OrderStatus.CREATED);
        order.setTotal(20.0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponseDto.class)))
                .thenReturn(OrderTestFactory.createPaymentResponseDto("FAILED"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponseDto result = orderService.processPayment(1L);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.id()).isEqualTo(1L);
                    assertThat(r.status()).isEqualTo(OrderStatus.PAYMENT_FAILED);
                });
        verify(orderRepository, times(1)).findById(1L);
        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(PaymentResponseDto.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when payment response is null")
    void processPayment_nullResponse_throwsIllegalStateException() {
        // Arrange
        Order order = OrderTestFactory.createOrder(OrderStatus.CREATED);
        order.setTotal(20.0);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentResponseDto.class))).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> orderService.processPayment(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Payment failed: No response");
        verify(orderRepository, times(1)).findById(1L);
        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(PaymentResponseDto.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw OrderStateException when order is not payable")
    void processPayment_invalidState_throwsOrderStateException() {
        // Arrange
        Order order = OrderTestFactory.createOrder(OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.processPayment(1L))
                .isInstanceOf(OrderStateException.class)
                .hasMessage("Order cannot be paid. It must be in CREATED or PAYMENT_FAILED state.");
        verify(orderRepository, times(1)).findById(1L);
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(PaymentResponseDto.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    // --- getOrderById ---
    @Test
    @DisplayName("Should return order when ID exists")
    void getOrderById_success() {
        // Arrange
        Order order = OrderTestFactory.createOrder(OrderStatus.CREATED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        OrderResponseDto result = orderService.getOrderById(1L);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.id()).isEqualTo(1L);
                    assertThat(r.status()).isEqualTo(OrderStatus.CREATED);
                });
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order ID does not exist")
    void getOrderById_notFound_throwsResourceNotFoundException() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found with ID: 1");
        verify(orderRepository, times(1)).findById(1L);
    }

    // --- getAllOrders ---
    @Test
    @DisplayName("Should return list of all orders")
    void getAllOrders_success() {
        // Arrange
        Order order = OrderTestFactory.createOrder(OrderStatus.CREATED);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        // Act
        List<OrderResponseDto> result = orderService.getAllOrders();

        // Assert
        assertThat(result)
                .hasSize(1)
                .satisfiesExactly(r -> assertThat(r.id()).isEqualTo(1L));
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no orders exist")
    void getAllOrders_empty() {
        // Arrange
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<OrderResponseDto> result = orderService.getAllOrders();

        // Assert
        assertThat(result).isEmpty();
        verify(orderRepository, times(1)).findAll();
    }

    // --- deleteOrder ---
    @Test
    @DisplayName("Should delete order successfully when in CREATED state")
    void deleteOrder_success() {
        // Arrange
        Order order = OrderTestFactory.createOrder(OrderStatus.CREATED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).delete(order);

        // Act
        orderService.deleteOrder(1L);

        // Assert
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    @DisplayName("Should throw OrderStateException when order is not in CREATED state")
    void deleteOrder_invalidState_throwsOrderStateException() {
        // Arrange
        Order order = OrderTestFactory.createOrder(OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.deleteOrder(1L))
                .isInstanceOf(OrderStateException.class)
                .hasMessage("Order can only be deleted if it is in CREATED state");
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).delete(any(Order.class));
    }
}
package com.rafaelgalvezg.orders.repository;

import com.rafaelgalvezg.orders.entity.Client;
import com.rafaelgalvezg.orders.entity.Order;
import com.rafaelgalvezg.orders.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByClientAndStatusIn(Client client, List<OrderStatus> statuses);
}
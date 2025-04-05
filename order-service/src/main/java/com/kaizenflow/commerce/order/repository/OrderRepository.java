package com.kaizenflow.commerce.order.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.kaizenflow.commerce.order.domain.model.Order;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByCustomerId(String customerId);

    List<Order> findByStatus(Order.OrderStatus status);

    List<Order> findByCustomerIdAndStatus(String customerId, Order.OrderStatus status);
}

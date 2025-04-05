package com.kaizenflow.commerce.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kaizenflow.commerce.order.domain.model.Order;
import com.kaizenflow.commerce.order.model.OrderRequestDTO;
import com.kaizenflow.commerce.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(OrderRequestDTO orderRequest) {
        Order order = new Order();
        order.setCustomerId(orderRequest.getCustomerId());
        order.setItems(orderRequest.getItems());
        order.setStatus(Order.OrderStatus.PENDING);

        // Calculate total amount
        BigDecimal totalAmount =
                orderRequest.getItems().stream()
                        .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(String orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersByCustomerAndStatus(String customerId, Order.OrderStatus status) {
        return orderRepository.findByCustomerIdAndStatus(customerId, status);
    }

    public Order updateOrder(String orderId, OrderRequestDTO orderRequest) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // Update items if provided
            if (orderRequest.getItems() != null) {
                order.setItems(orderRequest.getItems());

                // Recalculate total amount
                BigDecimal totalAmount =
                        orderRequest.getItems().stream()
                                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                order.setTotalAmount(totalAmount);
            }

            // Update timestamp
            order.setUpdatedAt(LocalDateTime.now());

            return orderRepository.save(order);
        }
        return null;
    }

    public Order updateOrderStatus(String orderId, Order.OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            return orderRepository.save(order);
        }
        return null;
    }

    public Order cancelOrder(String orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(Order.OrderStatus.CANCELLED);
            order.setUpdatedAt(LocalDateTime.now());
            return orderRepository.save(order);
        }
        return null;
    }
}

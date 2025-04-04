package com.kaizenflow.commerce.order.controller;

import com.kaizenflow.commerce.order.model.Order;
import com.kaizenflow.commerce.order.model.OrderRequest;
import com.kaizenflow.commerce.order.model.OrderStatusUpdateRequest;
import com.kaizenflow.commerce.order.model.OrderUpdateRequest;
import com.kaizenflow.commerce.order.model.PagedOrders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.kaizenflow.commerce.order.api.OrdersApi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderController implements OrdersApi {

    @Override
    public ResponseEntity<Order> cancelOrder(String orderId) {
        return OrdersApi.super.cancelOrder(orderId);
    }

    @Override
    public ResponseEntity<Order> createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber("order-" + UUID.randomUUID().toString());
        return ResponseEntity.ok(order);
    }

    @Override
    public ResponseEntity<Order> getOrderById(String orderId) {
        return OrdersApi.super.getOrderById(orderId);
    }

    @Override
    public ResponseEntity<PagedOrders> getOrderHistory(String customerId, Integer page, Integer size) {
        return OrdersApi.super.getOrderHistory(customerId, page, size);
    }

    @Override
    public ResponseEntity<PagedOrders> getOrders(String customerId, String status, Integer page, Integer size, String sort) {
        return OrdersApi.super.getOrders(customerId, status, page, size, sort);
    }

    @Override
    public ResponseEntity<Order> updateOrder(String orderId, OrderUpdateRequest orderUpdateRequest) {
        return OrdersApi.super.updateOrder(orderId, orderUpdateRequest);
    }

    @Override
    public ResponseEntity<Order> updateOrderStatus(String orderId, OrderStatusUpdateRequest orderStatusUpdateRequest) {
        return OrdersApi.super.updateOrderStatus(orderId, orderStatusUpdateRequest);
    }
}

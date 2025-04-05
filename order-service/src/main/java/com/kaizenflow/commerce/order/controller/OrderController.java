package com.kaizenflow.commerce.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.kaizenflow.commerce.order.api.OrdersApi;
import com.kaizenflow.commerce.order.mappers.OrderMapper;
import com.kaizenflow.commerce.order.model.OrderDTO;
import com.kaizenflow.commerce.order.model.OrderRequestDTO;
import com.kaizenflow.commerce.order.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class OrderController implements OrdersApi {
    private final OrderService orderService;

    private final OrderMapper orderMapper;

    @Autowired
    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @Override
    public ResponseEntity<Void> ordersGet(String customerId, String status) {
        return OrdersApi.super.ordersGet(customerId, status);
    }

    @Override
    public ResponseEntity<Void> ordersOrderIdDelete(String orderId) {
        return OrdersApi.super.ordersOrderIdDelete(orderId);
    }

    @Override
    public ResponseEntity<Void> ordersOrderIdGet(String orderId) {
        return OrdersApi.super.ordersOrderIdGet(orderId);
    }

    @Override
    public ResponseEntity<Void> ordersOrderIdPut(String orderId) {
        return OrdersApi.super.ordersOrderIdPut(orderId);
    }

    @Override
    public ResponseEntity<OrderDTO> ordersPost(OrderRequestDTO orderRequestDTO) {
        return new ResponseEntity<>(
                orderMapper.toDTO(orderService.createOrder(orderRequestDTO)), HttpStatus.CREATED);
    }
}

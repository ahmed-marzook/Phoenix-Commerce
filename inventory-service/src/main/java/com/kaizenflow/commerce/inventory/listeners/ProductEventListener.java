package com.kaizenflow.commerce.inventory.listeners;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.kaizenflow.commerce.inventory.service.InventoryService;
import com.kaizenflow.commerce.proto.product.ProductEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductEventListener {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = "${kafka.topic.product-events}",
            containerFactory = "productEventKafkaListenerContainerFactory")
    public void setProductEvent(ProductEvent productEvent) {
        log.info("Received Product event with {}", productEvent.getProduct());
        inventoryService.createInventory(productEvent);
    }
}

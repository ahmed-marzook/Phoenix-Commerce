package com.kaizenflow.commerce.product.listeners;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.kaizenflow.commerce.product.service.ProductService;
import com.kaizenflow.commerce.proto.inventory.InventoryUpdateEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryUpdateListener {
    private final ProductService productService;

    @KafkaListener(
            topics = "${kafka.topic.inventory-events}",
            containerFactory = "inventoryUpdateEventKafkaListenerContainerFactory")
    public void updateInventoryStatus(InventoryUpdateEvent inventoryUpdateEvent) {
        log.info(
                "Received Inventory Update event with productId: {}, available quantity: {}, status: {}",
                inventoryUpdateEvent.getProductId(),
                inventoryUpdateEvent.getAvailableQuantity(),
                inventoryUpdateEvent.getInventoryStatus());

        // Delegate to product service
        productService.updateProductInventory(
                inventoryUpdateEvent.getProductId(),
                inventoryUpdateEvent.getAvailableQuantity(),
                inventoryUpdateEvent.getInventoryStatus(),
                inventoryUpdateEvent.getInStock());
    }
}

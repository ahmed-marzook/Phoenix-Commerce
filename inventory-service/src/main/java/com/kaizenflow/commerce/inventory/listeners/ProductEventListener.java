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

    /** Listens for product creation events and creates initial inventory records. */
    @KafkaListener(
            topics = "${kafka.topic.product-created-events}",
            containerFactory = "productEventKafkaListenerContainerFactory")
    public void handleProductCreatedEvent(ProductEvent productEvent) {
        log.info(
                "Received Product created event with product ID: {}", productEvent.getProduct().getId());

        // Only process CREATED events
        if (productEvent.getType() == ProductEvent.EventType.CREATED) {
            inventoryService.createInventory(productEvent);
        }
    }
}

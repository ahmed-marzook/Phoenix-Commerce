package com.kaizenflow.commerce.inventory.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.Timestamp;
import com.kaizenflow.commerce.inventory.domain.enums.InventoryStatus;
import com.kaizenflow.commerce.proto.inventory.InventoryUpdateEvent;
import com.kaizenflow.commerce.proto.product.ProductEvent;
import com.kaizenflow.commerce.proto.product.ProductModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kaizenflow.commerce.inventory.domain.models.Inventory;
import com.kaizenflow.commerce.inventory.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, InventoryUpdateEvent> kafkaTemplate;

    @Value("${kafka.topic.inventory-events}")
    private String inventoryTopic;

    // Get all inventory items
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    // Get inventory by ID
    public Optional<Inventory> getInventoryById(String id) {
        return inventoryRepository.findById(id);
    }

    // Get inventory by product ID
    public Optional<Inventory> getInventoryByProductId(String productId) {
        return inventoryRepository.findByProductId(productId);
    }

    // Get inventory by product SKU
    public Optional<Inventory> getInventoryByProductSku(String productSku) {
        return inventoryRepository.findByProductSku(productSku);
    }

    // Get inventory by warehouse ID
    public List<Inventory> getInventoryByWarehouse(String warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId);
    }

    // Create new inventory
    public void createInventory(ProductEvent productEvent) {
        ProductModel product = productEvent.getProduct();
        // Check if productId or productSku already exists
        if (inventoryRepository.existsByProductId(product.getId())) {
            throw new IllegalArgumentException(
                    "Inventory with product ID " + product.getId() + " already exists");
        }
        if (inventoryRepository.existsByProductSku(product.getSku())) {
            throw new IllegalArgumentException(
                    "Inventory with product SKU " + product.getSku() + " already exists");
        }
        Inventory inventory = Inventory.builder().productId(product.getId()).productSku(product.getSku()).build();
        Inventory saved = inventoryRepository.save(inventory);

        Instant instant = Instant.now();
        Timestamp timestamp =
                Timestamp.newBuilder()
                        .setSeconds(instant.getEpochSecond())
                        .setNanos(instant.getNano())
                        .build();

        InventoryUpdateEvent.Builder builder = InventoryUpdateEvent.newBuilder();
        builder.setProductId(saved.getProductId());
        builder.setAvailableQuantity(saved.getAvailableQuantity());
        builder.setInventoryStatus(saved.getInventoryStatus().name());
        builder.setInStock(saved.getInStock());
        builder.setTimestamp(timestamp);

        kafkaTemplate.send(inventoryTopic, builder.build());
    }

    // Update inventory
    public Inventory updateInventory(String id, Inventory inventory) {
        Optional<Inventory> existingInventory = inventoryRepository.findById(id);
        if (existingInventory.isPresent()) {
            inventory.setId(id);
            return inventoryRepository.save(inventory);
        } else {
            throw new IllegalArgumentException("Inventory with ID " + id + " not found");
        }
    }

    // Delete inventory
    public void deleteInventory(String id) {
        if (inventoryRepository.existsById(id)) {
            inventoryRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Inventory with ID " + id + " not found");
        }
    }

    // Increment available quantity
    @Transactional
    public Inventory incrementAvailableQuantity(String id, Integer quantity) {
        Optional<Inventory> optionalInventory = inventoryRepository.findById(id);
        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
            return inventoryRepository.save(inventory);
        } else {
            throw new IllegalArgumentException("Inventory with ID " + id + " not found");
        }
    }

    // Decrement available quantity
    @Transactional
    public Inventory decrementAvailableQuantity(String id, Integer quantity) {
        Optional<Inventory> optionalInventory = inventoryRepository.findById(id);
        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            if (inventory.getAvailableQuantity() < quantity) {
                throw new IllegalArgumentException(
                        "Not enough available quantity in inventory with ID " + id);
            }
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
            return inventoryRepository.save(inventory);
        } else {
            throw new IllegalArgumentException("Inventory with ID " + id + " not found");
        }
    }

    // Reserve quantity (move from available to reserved)
    @Transactional
    public Inventory reserveQuantity(String id, Integer quantity) {
        Optional<Inventory> optionalInventory = inventoryRepository.findById(id);
        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            if (inventory.getAvailableQuantity() < quantity) {
                throw new IllegalArgumentException(
                        "Not enough available quantity to reserve in inventory with ID " + id);
            }
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
            inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
            return inventoryRepository.save(inventory);
        } else {
            throw new IllegalArgumentException("Inventory with ID " + id + " not found");
        }
    }

    // Release reserved quantity (move from reserved to available)
    @Transactional
    public Inventory releaseReservedQuantity(String id, Integer quantity) {
        Optional<Inventory> optionalInventory = inventoryRepository.findById(id);
        if (optionalInventory.isPresent()) {
            Inventory inventory = optionalInventory.get();
            if (inventory.getReservedQuantity() < quantity) {
                throw new IllegalArgumentException(
                        "Not enough reserved quantity to release in inventory with ID " + id);
            }
            inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
            return inventoryRepository.save(inventory);
        } else {
            throw new IllegalArgumentException("Inventory with ID " + id + " not found");
        }
    }

    // Get low stock inventory (available quantity below threshold)
    public List<Inventory> getLowStockInventory(Integer threshold) {
        return inventoryRepository.findByAvailableQuantityLessThan(threshold);
    }

    private InventoryStatus determineInventoryStatus(int quantity, int reserved) {
        int available = quantity - reserved;
        if (available <= 0) return InventoryStatus.OUT_OF_STOCK;
        if (available < 10) return InventoryStatus.LOW_STOCK; // Threshold could be configurable
        return InventoryStatus.IN_STOCK;
    }
}

package com.kaizenflow.commerce.inventory.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.protobuf.Timestamp;
import com.kaizenflow.commerce.inventory.domain.enums.InventoryStatus;
import com.kaizenflow.commerce.inventory.domain.models.Inventory;
import com.kaizenflow.commerce.inventory.repository.InventoryRepository;
import com.kaizenflow.commerce.proto.inventory.InventoryUpdateEvent;
import com.kaizenflow.commerce.proto.product.ProductEvent;
import com.kaizenflow.commerce.proto.product.ProductModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, InventoryUpdateEvent> kafkaTemplate;

    @Value("${kafka.topic.inventory-events}")
    private String inventoryTopic;

    // Get all inventory items
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    // Get inventory by product ID
    public Optional<Inventory> getInventoryByProductId(String productId) {
        return inventoryRepository.findByProductId(productId);
    }

    // Get inventory by product SKU
    public Optional<Inventory> getInventoryByProductSku(String productSku) {
        return inventoryRepository.findByProductSku(productSku);
    }

    public Optional<Inventory> getInventoryById(String id) {
        return inventoryRepository.findById(id);
    }

    // Get inventory by warehouse ID
    public List<Inventory> getInventoryByWarehouse(String warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId);
    }

    /**
     * Creates a new inventory entry for a product and sends an update event.
     *
     * @param productEvent The product event containing product information
     * @return The created inventory entity
     * @throws IllegalArgumentException if inventory with the same product ID or SKU already exists
     */
    public Inventory createInventory(ProductEvent productEvent) {
        ProductModel product = productEvent.getProduct();

        // Validate unique constraints
        validateUniqueInventory(product.getId(), product.getSku());

        // Build and save inventory entity
        Inventory inventory = buildInventoryFromProduct(product);
        Inventory saved = inventoryRepository.save(inventory);

        // Send inventory update event
        sendInventoryUpdateEvent(saved);

        return saved;
    }

    /**
     * Updates an existing inventory with a new quantity and sends an update event. Uses the inventory
     * ID to find the inventory record.
     *
     * @param id The inventory ID
     * @param availableQuantity The new available quantity
     * @return The updated inventory entity
     * @throws IllegalArgumentException if inventory with the given ID is not found
     */
    public Inventory updateInventory(String id, Integer availableQuantity) {
        // Find and validate inventory exists
        Inventory inventory = findInventoryById(id);

        // Update inventory with new quantity
        updateInventoryQuantity(inventory, availableQuantity);

        // Save changes
        Inventory saved = inventoryRepository.save(inventory);

        // Send inventory update event
        sendInventoryUpdateEvent(saved);

        return saved;
    }

    /**
     * Updates an existing inventory with a new quantity and sends an update event. Uses the product
     * SKU to find the inventory record.
     *
     * @param productSku The product SKU
     * @param availableQuantity The new available quantity
     * @return The updated inventory entity
     * @throws IllegalArgumentException if inventory with the given product SKU is not found
     */
    public Inventory updateInventoryByProductSku(String productSku, Integer availableQuantity) {
        // Find and validate inventory exists by product SKU
        Inventory inventory = findInventoryByProductSku(productSku);

        // Update inventory with new quantity
        updateInventoryQuantity(inventory, availableQuantity);

        // Save changes
        Inventory saved = inventoryRepository.save(inventory);

        // Send inventory update event
        sendInventoryUpdateEvent(saved);

        return saved;
    }

    /**
     * Validates that no inventory exists with the given product ID or SKU.
     *
     * @param productId The product ID to check
     * @param productSku The product SKU to check
     * @throws IllegalArgumentException if inventory with the given product ID or SKU already exists
     */
    private void validateUniqueInventory(String productId, String productSku) {
        if (inventoryRepository.existsByProductId(productId)) {
            throw new IllegalArgumentException(
                    "Inventory with product ID " + productId + " already exists");
        }
        if (inventoryRepository.existsByProductSku(productSku)) {
            throw new IllegalArgumentException(
                    "Inventory with product SKU " + productSku + " already exists");
        }
    }

    /**
     * Builds a new Inventory entity from a product model.
     *
     * @param product The product model containing product information
     * @return A new unsaved Inventory entity
     */
    private Inventory buildInventoryFromProduct(ProductModel product) {
        return Inventory.builder()
                .productId(product.getId())
                .productSku(product.getSku())
                .availableQuantity(0) // Default to 0
                .inventoryStatus(InventoryStatus.OUT_OF_STOCK) // Default status
                .inStock(false) // Default to not in stock
                .build();
    }

    /**
     * Finds an inventory by ID or throws an exception if not found.
     *
     * @param id The inventory ID
     * @return The found inventory entity
     * @throws IllegalArgumentException if inventory with the given ID is not found
     */
    private Inventory findInventoryById(String id) {
        return getInventoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory with ID " + id + " not found"));
    }

    /**
     * Finds an inventory by product SKU or throws an exception if not found.
     *
     * @param productSku The product SKU
     * @return The found inventory entity
     * @throws IllegalArgumentException if inventory with the given product SKU is not found
     */
    private Inventory findInventoryByProductSku(String productSku) {
        return getInventoryByProductSku(productSku)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Inventory with product SKU " + productSku + " not found"));
    }

    /**
     * Updates an inventory entity with a new quantity and recalculates status.
     *
     * @param inventory The inventory entity to update
     * @param availableQuantity The new available quantity
     */
    private void updateInventoryQuantity(Inventory inventory, Integer availableQuantity) {
        inventory.setAvailableQuantity(availableQuantity);

        // Update inventory status based on quantity
        if (availableQuantity <= 0) {
            inventory.setInventoryStatus(InventoryStatus.OUT_OF_STOCK);
            inventory.setInStock(false);
        } else if (availableQuantity <= 5) { // Using 5 as threshold for low stock
            inventory.setInventoryStatus(InventoryStatus.LOW_STOCK);
            inventory.setInStock(true);
        } else {
            inventory.setInventoryStatus(InventoryStatus.IN_STOCK);
            inventory.setInStock(true);
        }
    }

    /**
     * Creates and sends an inventory update event to Kafka.
     *
     * @param inventory The inventory entity to create an event for
     */
    private void sendInventoryUpdateEvent(Inventory inventory) {
        Instant instant = Instant.now();
        Timestamp timestamp =
                Timestamp.newBuilder()
                        .setSeconds(instant.getEpochSecond())
                        .setNanos(instant.getNano())
                        .build();

        InventoryUpdateEvent.Builder builder = InventoryUpdateEvent.newBuilder();
        builder.setProductId(inventory.getProductId());
        builder.setAvailableQuantity(inventory.getAvailableQuantity());
        builder.setInventoryStatus(inventory.getInventoryStatus().name());
        builder.setInStock(inventory.getInStock());
        builder.setTimestamp(timestamp);

        // Send event to Kafka
        kafkaTemplate.send(inventoryTopic, builder.build());

        log.info(
                "Sent inventory update event for product ID: {}, status: {}, quantity: {}",
                inventory.getProductId(),
                inventory.getInventoryStatus(),
                inventory.getAvailableQuantity());
    }

    // Delete inventory
    public void deleteInventory(String id) {
        if (inventoryRepository.existsById(id)) {
            inventoryRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Inventory with ID " + id + " not found");
        }
    }

    // Delete inventory by product SKU
    public void deleteInventoryByProductSku(String productSku) {
        Optional<Inventory> optionalInventory = inventoryRepository.findByProductSku(productSku);
        if (optionalInventory.isPresent()) {
            inventoryRepository.delete(optionalInventory.get());
        } else {
            throw new IllegalArgumentException("Inventory with product SKU " + productSku + " not found");
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

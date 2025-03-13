package com.kaizenflow.commerce.inventory.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kaizenflow.commerce.inventory.domain.models.Inventory;
import com.kaizenflow.commerce.inventory.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

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
    public Inventory createInventory(Inventory inventory) {
        // Check if productId or productSku already exists
        if (inventoryRepository.existsByProductId(inventory.getProductId())) {
            throw new IllegalArgumentException(
                    "Inventory with product ID " + inventory.getProductId() + " already exists");
        }
        if (inventoryRepository.existsByProductSku(inventory.getProductSku())) {
            throw new IllegalArgumentException(
                    "Inventory with product SKU " + inventory.getProductSku() + " already exists");
        }
        return inventoryRepository.save(inventory);
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
}

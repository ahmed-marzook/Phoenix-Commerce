package com.kaizenflow.commerce.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.kaizenflow.commerce.inventory.domain.models.Inventory;

@Repository
public interface InventoryRepository extends MongoRepository<Inventory, String> {

    // Find by productId
    Optional<Inventory> findByProductId(String productId);

    // Find by productSku
    Optional<Inventory> findByProductSku(String productSku);

    // Find all inventory items in a specific warehouse
    List<Inventory> findByWarehouseId(String warehouseId);

    // Find all inventory items with available quantity below threshold
    List<Inventory> findByAvailableQuantityLessThan(Integer threshold);

    // Find all inventory items with available quantity greater than or equal to threshold
    List<Inventory> findByAvailableQuantityGreaterThanEqual(Integer threshold);

    // Check if product exists by productId
    boolean existsByProductId(String productId);

    // Check if product exists by productSku
    boolean existsByProductSku(String productSku);
}

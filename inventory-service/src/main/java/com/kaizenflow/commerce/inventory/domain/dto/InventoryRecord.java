package com.kaizenflow.commerce.inventory.domain.dto;

import java.time.LocalDateTime;

import com.kaizenflow.commerce.inventory.domain.enums.InventoryStatus;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object (DTO) for Inventory information. Used for returning inventory data in API
 * responses.
 */
@Schema(description = "Inventory information")
public record InventoryRecord(
        @Schema(
                        description = "Unique identifier for the inventory",
                        example = "5f9b3c7e8d6a4b2f1e0c9d8a")
                String id,
        @Schema(
                        description = "Product identifier that this inventory belongs to",
                        example = "prod-12345")
                String productId,
        @Schema(description = "Product SKU", example = "SKU-001-ABC") String productSku,
        @Schema(description = "Available quantity in stock", example = "150") Integer availableQuantity,
        @Schema(
                        description = "Inventory status (IN_STOCK, LOW_STOCK, OUT_OF_STOCK)",
                        example = "IN_STOCK")
                InventoryStatus inventoryStatus,
        @Schema(description = "Whether the product is in stock", example = "true") Boolean inStock,
        @Schema(description = "Number of items reserved for pending orders", example = "10")
                Integer reservedQuantity,
        @Schema(description = "Date and time when inventory was last updated")
                LocalDateTime lastUpdated,
        @Schema(description = "Date and time when inventory was created") LocalDateTime createdAt,
        @Schema(description = "Warehouse location code", example = "WH-EAST-01") String warehouseCode,
        @Schema(description = "Inventory shelf location", example = "A12-B3") String shelfLocation) {}

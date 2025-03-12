package com.kaizenflow.commerce.product.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.kaizenflow.commerce.product.domain.enums.InventoryStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Product class represents a product entity in a MongoDB database. This class uses: - Lombok
 * annotations (@Data, @Builder, etc.) to reduce boilerplate code - Spring Data MongoDB annotations
 * to map to a MongoDB collection - Spring Data auditing features for tracking creation and
 * modification times
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    @Id private String id; // MongoDB: Marks this field as the document identifier

    @Indexed private String name; // MongoDB: Creates an index on this field for faster queries

    private String description; // Product description text

    private BigDecimal price; // Sale price to customers

    @Indexed(unique = true)
    private String sku; // MongoDB: Creates a unique index on SKU (Stock Keeping Unit)

    private BigDecimal costPrice; // Cost price (for calculating margins)

    @Indexed private String category; // MongoDB: Creates an index for category-based queries

    private List<String> tags; // List of tags/keywords associated with the product

    private String brand; // Product manufacturer/brand name

    private Boolean active; // Flag indicating if product is active in the system

    // Optional - additional cached inventory info
    private Boolean inStock; // Flag indicating if product is currently in stock

    private InventoryStatus
            inventoryStatus; // Status description (e.g., "In Stock", "Low Stock", "Out of Stock")

    // Cached inventory data
    private Integer availableQuantity; // Current available quantity

    private LocalDateTime inventoryLastUpdated; // Timestamp of last inventory update

    @CreatedDate
    private LocalDateTime createdAt; // Spring Data: Automatically sets creation timestamp

    @LastModifiedDate
    private LocalDateTime updatedAt; // Spring Data: Automatically updates on modifications
}

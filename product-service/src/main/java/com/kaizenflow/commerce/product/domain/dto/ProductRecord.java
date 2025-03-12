package com.kaizenflow.commerce.product.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductRecord(
        String id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        BigDecimal costPrice,
        String category,
        List<String> tags,
        String brand,
        Boolean active,
        Boolean inStock,
        String inventoryStatus,
        Integer availableQuantity,
        LocalDateTime inventoryLastUpdated,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

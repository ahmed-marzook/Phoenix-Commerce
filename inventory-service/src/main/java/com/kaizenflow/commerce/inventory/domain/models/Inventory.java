package com.kaizenflow.commerce.inventory.domain.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.kaizenflow.commerce.inventory.domain.enums.InventoryStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "inventory")
public class Inventory {
    @Id private String id;

    @Indexed(unique = true)
    private String productId;

    @Indexed(unique = true)
    private String productSku;

    @Builder.Default private Integer availableQuantity = 0;
    @Builder.Default private Integer reservedQuantity = 0;
    private String warehouseId;

    @Builder.Default private InventoryStatus inventoryStatus = InventoryStatus.OUT_OF_STOCK;

    @Builder.Default private Boolean inStock = Boolean.FALSE;

    @CreatedDate
    private LocalDateTime createdAt; // Spring Data: Automatically sets creation timestamp

    @LastModifiedDate
    private LocalDateTime updatedAt; // Spring Data: Automatically updates on modifications
}

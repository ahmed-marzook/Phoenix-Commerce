package com.kaizenflow.commerce.inventory.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "inventory")
public class Inventory {
    @Id
    private String id;
    @Indexed(unique = true)
    private String productId;
    @Indexed(unique = true)
    private String productSku;
    @Builder.Default private Integer availableQuantity = 0;
    @Builder.Default private Integer reservedQuantity = 0;
    private String warehouseId;
    @CreatedDate
    private LocalDateTime createdAt; // Spring Data: Automatically sets creation timestamp

    @LastModifiedDate
    private LocalDateTime updatedAt; // Spring Data: Automatically updates on modifications
}

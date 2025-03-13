package com.kaizenflow.commerce.inventory.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Request DTO for updating inventory quantity. */
public record UpdateInventoryRequest(
        @Schema(description = "Available quantity of the product", example = "100")
                @NotNull(message = "Available quantity must not be null")
                @Min(value = 0, message = "Available quantity must be greater than or equal to 0")
                Integer availableQuantity) {}

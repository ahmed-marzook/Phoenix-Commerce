package com.kaizenflow.commerce.inventory.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.kaizenflow.commerce.inventory.domain.dto.InventoryRecord;
import com.kaizenflow.commerce.inventory.domain.models.Inventory;

/**
 * Mapper interface for converting between Inventory entity and InventoryRecord DTO. Uses MapStruct
 * for automatic implementation of mapping methods.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryMapper {

    /**
     * Converts an Inventory entity to an InventoryRecord DTO.
     *
     * @param inventory The inventory entity
     * @return The inventory record DTO
     */
    InventoryRecord inventoryToInventoryRecord(Inventory inventory);

    /**
     * Converts an InventoryRecord DTO to an Inventory entity. Note: This is typically not used for
     * direct API operations but might be useful for testing.
     *
     * @param inventoryRecord The inventory record DTO
     * @return The inventory entity
     */
    Inventory inventoryRecordToInventory(InventoryRecord inventoryRecord);
}

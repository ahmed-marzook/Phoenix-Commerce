package com.kaizenflow.commerce.inventory.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kaizenflow.commerce.inventory.domain.dto.InventoryRecord;
import com.kaizenflow.commerce.inventory.domain.dto.UpdateInventoryRequest;
import com.kaizenflow.commerce.inventory.domain.models.Inventory;
import com.kaizenflow.commerce.inventory.mappers.InventoryMapper;
import com.kaizenflow.commerce.inventory.service.InventoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable String id) {
        return ResponseEntity.ok(inventoryService.findInventoryById(id));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Inventory> getInventoryByProductId(@PathVariable String productId) {
        return inventoryService
                .getInventoryByProductId(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sku/{productSku}")
    public ResponseEntity<Inventory> getInventoryByProductSku(@PathVariable String productSku) {
        return inventoryService
                .getInventoryByProductSku(productSku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<Inventory>> getInventoryByWarehouse(@PathVariable String warehouseId) {
        List<Inventory> inventoryList = inventoryService.getInventoryByWarehouse(warehouseId);
        return ResponseEntity.ok(inventoryList);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockInventory(
            @RequestParam(defaultValue = "10") Integer threshold) {
        return ResponseEntity.ok(inventoryService.getLowStockInventory(threshold));
    }

    /**
     * Updates the quantity of an existing inventory.
     *
     * @param id The inventory ID
     * @param request The update request containing the new quantity
     * @return ResponseEntity with updated inventory data
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Update inventory quantity",
            description =
                    "Updates the quantity of an existing inventory and returns the updated inventory data")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Successfully updated inventory"),
                @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                @ApiResponse(responseCode = "404", description = "Inventory not found"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public ResponseEntity<InventoryRecord> updateInventory(
            @Parameter(description = "Inventory ID", required = true) @PathVariable String id,
            @Parameter(description = "Update inventory request", required = true) @Valid @RequestBody
                    UpdateInventoryRequest request) {

        log.info(
                "Received request to update inventory with ID: {}, new quantity: {}",
                id,
                request.availableQuantity());

        try {
            // Call service to update inventory
            Inventory updatedInventory =
                    inventoryService.updateInventory(id, request.availableQuantity());

            // Map to DTO and return
            InventoryRecord response = inventoryMapper.inventoryToInventoryRecord(updatedInventory);

            log.info("Successfully updated inventory with ID: {}", id);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Handle case where inventory is not found
            log.error("Failed to update inventory: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Handle other unexpected errors
            log.error("Error updating inventory", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable String id) {
        try {
            inventoryService.deleteInventory(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/increment")
    public ResponseEntity<Inventory> incrementAvailableQuantity(
            @PathVariable String id, @RequestParam Integer quantity) {
        try {
            Inventory inventory = inventoryService.incrementAvailableQuantity(id, quantity);
            return ResponseEntity.ok(inventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/decrement")
    public ResponseEntity<Inventory> decrementAvailableQuantity(
            @PathVariable String id, @RequestParam Integer quantity) {
        try {
            Inventory inventory = inventoryService.decrementAvailableQuantity(id, quantity);
            return ResponseEntity.ok(inventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/reserve")
    public ResponseEntity<Inventory> reserveQuantity(
            @PathVariable String id, @RequestParam Integer quantity) {
        try {
            Inventory inventory = inventoryService.reserveQuantity(id, quantity);
            return ResponseEntity.ok(inventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/release")
    public ResponseEntity<Inventory> releaseReservedQuantity(
            @PathVariable String id, @RequestParam Integer quantity) {
        try {
            Inventory inventory = inventoryService.releaseReservedQuantity(id, quantity);
            return ResponseEntity.ok(inventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

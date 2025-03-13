package com.kaizenflow.commerce.inventory.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kaizenflow.commerce.inventory.domain.models.Inventory;
import com.kaizenflow.commerce.inventory.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable String id) {
        return inventoryService
                .getInventoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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

    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {
        try {
            Inventory createdInventory = inventoryService.createInventory(inventory);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(
            @PathVariable String id, @RequestBody Inventory inventory) {
        try {
            Inventory updatedInventory = inventoryService.updateInventory(id, inventory);
            return ResponseEntity.ok(updatedInventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
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

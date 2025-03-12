package com.kaizenflow.commerce.product.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.kaizenflow.commerce.product.domain.dto.ProductRecord;
import com.kaizenflow.commerce.product.domain.models.Product;

/**
 * Repository interface for Product entity Extends MongoRepository to inherit basic CRUD operations
 * and custom query methods
 */
@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // Find a product by its SKU
    Optional<ProductRecord> findBySku(String sku);

    // Find products by name (case-insensitive containing match)
    List<ProductRecord> findByNameContainingIgnoreCase(String name);

    // Find products by category
    List<ProductRecord> findByCategory(String category);

    // Find products by brand
    List<ProductRecord> findByBrand(String brand);

    // Find active products only
    List<ProductRecord> findByActiveTrue();

    // Find in-stock products only
    List<ProductRecord> findByInStockTrue();

    // Find products by price range
    List<ProductRecord> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Find products by multiple tags
    List<ProductRecord> findByTagsIn(List<String> tags);

    // Find recently updated products
    List<ProductRecord> findByUpdatedAtAfter(LocalDateTime date);

    // Find low inventory products (custom query)
    @Query("{ 'availableQuantity': { $lt: ?0 }, 'active': true }")
    List<ProductRecord> findLowInventoryProducts(int threshold);

    // Find products by inventory status
    List<ProductRecord> findByInventoryStatus(String status);

    // Count products by category
    long countByCategory(String category);

    // Check if a product exists by SKU
    boolean existsBySku(String sku);

    // Custom query to find products by multiple criteria
    @Query("{ 'category': ?0, 'active': true, 'price': { $lt: ?1 } }")
    List<ProductRecord> findActiveByCategoryAndPriceLessThan(String category, BigDecimal price);

    // Delete products by brand
    void deleteByBrand(String brand);
}

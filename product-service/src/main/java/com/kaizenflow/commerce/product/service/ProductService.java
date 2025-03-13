package com.kaizenflow.commerce.product.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.protobuf.Timestamp;
import com.kaizenflow.commerce.product.domain.dto.ProductRecord;
import com.kaizenflow.commerce.product.domain.dto.request.CreateProductRequest;
import com.kaizenflow.commerce.product.domain.enums.InventoryStatus;
import com.kaizenflow.commerce.product.domain.models.Product;
import com.kaizenflow.commerce.product.mappers.ProductMapper;
import com.kaizenflow.commerce.product.repository.ProductRepository;
import com.kaizenflow.commerce.proto.product.ProductEvent;
import com.kaizenflow.commerce.proto.product.ProductModel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductService {
    private final ProductRepository repository;
    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;
    private final ProductMapper productMapper;

    @Value("${kafka.topic.product-created-events}")
    private String productCreatedEventsTopic;

    @Value("${kafka.topic.product-updated-events}")
    private String productUpdatedEventsTopic;

    @Autowired
    public ProductService(
            ProductRepository repository,
            KafkaTemplate<String, ProductEvent> kafkaTemplate,
            ProductMapper productMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.productMapper = productMapper;
    }

    /**
     * Creates a new product from the request and publishes a creation event.
     *
     * @param createProductRequest The product creation request
     * @return The created product as a ProductRecord
     */
    @Transactional
    public ProductRecord createProduct(CreateProductRequest createProductRequest) {
        // Convert request to product entity
        Product newProduct = createProductFromRequest(createProductRequest);

        // Save product to repository
        Product saved = repository.save(newProduct);

        // Publish product created event
        publishProductEvent(saved, ProductEvent.EventType.CREATED);

        // Return as DTO
        return productMapper.productToProductRecord(saved);
    }

    /**
     * Creates a Product entity from a CreateProductRequest.
     *
     * @param request The product creation request
     * @return A new unsaved Product entity
     */
    private Product createProductFromRequest(CreateProductRequest request) {
        return Product.builder()
                .sku(UUID.randomUUID())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .costPrice(request.costPrice())
                .category(request.category())
                .tags(request.tags())
                .brand(request.brand())
                .active(Boolean.TRUE)
                .inStock(Boolean.FALSE) // Default to false until inventory is updated
                .inventoryStatus(InventoryStatus.OUT_OF_STOCK) // Default status
                .availableQuantity(0) // Default quantity
                .build();
    }

    /**
     * Publishes a product event to Kafka with the specified event type. Uses different topics based
     * on the event type.
     *
     * @param product The product to publish
     * @param eventType The type of event (CREATED, UPDATED, etc.)
     */
    private void publishProductEvent(Product product, ProductEvent.EventType eventType) {
        // Convert product to Protobuf model
        ProductModel protoProduct = convertToProtoProduct(product);

        // Create timestamp
        Timestamp timestamp = createTimestamp();

        // Build event
        ProductEvent event =
                ProductEvent.newBuilder()
                        .setType(eventType)
                        .setTimestamp(timestamp)
                        .setProduct(protoProduct)
                        .build();

        // Choose the appropriate topic based on event type
        String topicName;
        if (eventType == ProductEvent.EventType.CREATED) {
            topicName = productCreatedEventsTopic;
        } else {
            topicName = productUpdatedEventsTopic;
        }

        // Publish to the selected Kafka topic
        kafkaTemplate.send(topicName, product.getId(), event);

        log.info(
                "Published {} event for product ID: {} to topic: {}",
                eventType,
                product.getId(),
                topicName);
    }

    /**
     * Converts a Product entity to a ProductModel protobuf message.
     *
     * @param product The product entity
     * @return A ProductModel protobuf message
     */
    private ProductModel convertToProtoProduct(Product product) {
        return ProductModel.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setDescription(product.getDescription())
                .setPrice(product.getPrice().doubleValue())
                .setCategory(product.getCategory())
                .setInStock(product.getInStock())
                .setSku(product.getSku().toString())
                .build();
    }

    /**
     * Creates a Protobuf Timestamp for the current time.
     *
     * @return A Protobuf Timestamp
     */
    private Timestamp createTimestamp() {
        Instant instant = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    /**
     * Updates a product's inventory information based on an inventory update event.
     *
     * @param productId The ID of the product to update
     * @param availableQuantity The new available quantity
     * @param inventoryStatusStr The inventory status as a string from the event
     * @param inStock Whether the product is in stock
     * @return true if product was updated successfully, false otherwise
     */
    @Transactional
    public boolean updateProductInventory(
            String productId, int availableQuantity, String inventoryStatusStr, boolean inStock) {
        // Find product by ID
        Optional<Product> optionalProduct = repository.findById(productId);

        if (optionalProduct.isEmpty()) {
            log.warn("Product not found with ID: {}", productId);
            return false;
        }

        Product product = optionalProduct.get();

        // Convert the string inventory status to enum
        InventoryStatus inventoryStatus;
        try {
            inventoryStatus = InventoryStatus.valueOf(inventoryStatusStr);
        } catch (IllegalArgumentException e) {
            log.warn(
                    "Invalid inventory status: {}. Using calculated status instead.", inventoryStatusStr);
            inventoryStatus = determineInventoryStatus(availableQuantity);
        }

        // Update product
        product.setAvailableQuantity(availableQuantity);
        product.setInventoryStatus(inventoryStatus);
        product.setInStock(inStock);
        product.setInventoryLastUpdated(LocalDateTime.now());

        // Save updated product
        repository.save(product);

        log.info(
                "Product {} inventory updated. Status: {}, Quantity: {}, InStock: {}",
                product.getId(),
                inventoryStatus,
                availableQuantity,
                inStock);

        // No need to publish a product update event here to avoid circular events
        // The inventory service is already aware of this update since it initiated it

        return true;
    }

    /**
     * Determines the inventory status based on the available quantity.
     *
     * @param availableQuantity The current available quantity
     * @return The appropriate inventory status
     */
    private InventoryStatus determineInventoryStatus(int availableQuantity) {
        if (availableQuantity <= 0) {
            return InventoryStatus.OUT_OF_STOCK;
        } else if (availableQuantity <= 5) { // Using 5 as a threshold for low stock
            return InventoryStatus.LOW_STOCK;
        } else {
            return InventoryStatus.IN_STOCK;
        }
    }
}

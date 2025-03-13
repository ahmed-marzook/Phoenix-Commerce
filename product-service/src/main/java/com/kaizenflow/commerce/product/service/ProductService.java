package com.kaizenflow.commerce.product.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.google.protobuf.Timestamp;
import com.kaizenflow.commerce.product.domain.dto.ProductRecord;
import com.kaizenflow.commerce.product.domain.dto.request.CreateProductRequest;
import com.kaizenflow.commerce.product.domain.models.Product;
import com.kaizenflow.commerce.product.mappers.ProductMapper;
import com.kaizenflow.commerce.product.repository.ProductRepository;
import com.kaizenflow.commerce.proto.product.ProductEvent;
import com.kaizenflow.commerce.proto.product.ProductModel;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository repository;
    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;
    private final ProductMapper productMapper;

    @Value("${kafka.topic.product-events}")
    private String productTopic;

    @Autowired
    public ProductService(
            ProductRepository repository,
            KafkaTemplate<String, ProductEvent> kafkaTemplate,
            ProductMapper productMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductRecord createProduct(CreateProductRequest createProductRequest) {
        Product newProduct =
                Product.builder()
                        .sku(UUID.randomUUID())
                        .name(createProductRequest.name())
                        .description(createProductRequest.description())
                        .price(createProductRequest.price())
                        .category(createProductRequest.category())
                        .tags(createProductRequest.tags())
                        .brand(createProductRequest.brand())
                        .build();
        Product saved = repository.save(newProduct);

        // Create Protobuf message
        ProductModel protoProduct =
                ProductModel.newBuilder()
                        .setId(saved.getId())
                        .setName(saved.getName())
                        .setDescription(saved.getDescription())
                        .setPrice(saved.getPrice().doubleValue())
                        .setCategory(saved.getCategory())
                        .setInStock(saved.getInStock())
                        .setSku(saved.getSku().toString())
                        .build();

        Instant instant = Instant.now();
        Timestamp timestamp =
                Timestamp.newBuilder()
                        .setSeconds(instant.getEpochSecond())
                        .setNanos(instant.getNano())
                        .build();

        ProductEvent event =
                ProductEvent.newBuilder()
                        .setType(ProductEvent.EventType.CREATED)
                        .setTimestamp(timestamp)
                        .setProduct(protoProduct)
                        .build();

        // Publish to Kafka
        kafkaTemplate.send(productTopic, saved.getId(), event);

        return productMapper.productToProductRecord(saved);
    }
}

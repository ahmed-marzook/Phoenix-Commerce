package com.kaizenflow.commerce.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kaizenflow.commerce.product.domain.dto.ProductRecord;
import com.kaizenflow.commerce.product.domain.dto.request.CreateProductRequest;
import com.kaizenflow.commerce.product.service.ProductService;

import jakarta.validation.Valid;

/** REST Controller for handling product-related operations. */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Creates a new product based on the provided request.
     *
     * @param createProductRequest The request containing product details
     * @return ResponseEntity containing the created product record
     */
    @PostMapping
    public ResponseEntity<ProductRecord> createProduct(
            @Valid @RequestBody CreateProductRequest createProductRequest) {
        ProductRecord createdProduct = productService.createProduct(createProductRequest);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }
}

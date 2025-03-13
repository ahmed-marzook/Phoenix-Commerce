package com.kaizenflow.commerce.product.domain.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductRequest(
        String name,
        String description,
        BigDecimal price,
        BigDecimal costPrice,
        String category,
        List<String> tags,
        String brand) {}

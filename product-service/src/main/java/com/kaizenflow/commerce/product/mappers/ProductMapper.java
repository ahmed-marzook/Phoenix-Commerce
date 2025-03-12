package com.kaizenflow.commerce.product.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.kaizenflow.commerce.product.domain.dto.ProductRecord;
import com.kaizenflow.commerce.product.domain.models.Product;

/**
 * MapStruct mapper for converting between Product entity and ProductRecord.
 *
 * <p>This interface will be implemented automatically by MapStruct during compile-time to generate
 * efficient mapping code between the entity and Record.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
public interface ProductMapper {

    /**
     * Maps a Product entity to a ProductRecord.
     *
     * @param product the source Product entity
     * @return a mapped ProductRecord
     */
    ProductRecord productToProductRecord(Product product);

    /**
     * Maps a ProductRecord to a Product entity.
     *
     * @param productRecord the source ProductRecord
     * @return a mapped Product entity
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product productRecordToProduct(ProductRecord productRecord);
}

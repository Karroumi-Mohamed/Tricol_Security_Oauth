package com.tricol.mappers;

import com.tricol.dtos.request.ProductRequest;
import com.tricol.dtos.response.ProductResponse;
import com.tricol.entities.Product;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toEntity(ProductRequest request);

    @Mapping(target = "belowReorderLevel", ignore = true)
    ProductResponse toResponse(Product product);

    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);

    @AfterMapping
    default void calculateBelowReorderLevel(Product product, @MappingTarget ProductResponse response) {
        if (product.getReorderLevel() != null && product.getCurrentStock() != null) {
            response.setBelowReorderLevel(product.getCurrentStock().compareTo(product.getReorderLevel()) <= 0);
        } else {
            response.setBelowReorderLevel(false);
        }
    }
}

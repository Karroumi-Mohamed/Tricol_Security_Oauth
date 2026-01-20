package com.tricol.mappers;

import com.tricol.dtos.response.OrderLineResponse;
import com.tricol.dtos.response.OrderResponse;
import com.tricol.entities.Order;
import com.tricol.entities.OrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {SupplierMapper.class})
public interface OrderMapper {

    @Mapping(target = "lines", source = "lines")
    @Mapping(target = "status", source = "orderStatus")
    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productReference", source = "product.reference")
    @Mapping(target = "productName", source = "product.name")
    // If unitOfMeasure exists in response but not mapped, it would warn. If no warning, maybe it's not there.
    OrderLineResponse toLineResponse(OrderLine line);
}

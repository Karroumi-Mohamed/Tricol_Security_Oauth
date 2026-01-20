package com.tricol.mappers;

import com.tricol.dtos.response.StockAlertResponse;
import com.tricol.dtos.response.StockLotResponse;
import com.tricol.dtos.response.StockMovementResponse;
import com.tricol.entities.Product;
import com.tricol.entities.StockLot;
import com.tricol.entities.StockMovement;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface StockMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productReference", source = "product.reference")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    @Mapping(target = "lotValue", expression = "java(lot.getRemainingQuantity().multiply(lot.getPurchasePrice()))")
    StockLotResponse toStockLotResponse(StockLot lot);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productReference", source = "product.reference")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "stockLotId", source = "stockLot.id")
    @Mapping(target = "lotNumber", source = "stockLot.lotNumber")
    StockMovementResponse toStockMovementResponse(StockMovement movement);

    @Mapping(target = "productId", source = "id")
    @Mapping(target = "productReference", source = "reference")
    @Mapping(target = "productName", source = "name")
    @Mapping(target = "deficit", ignore = true)
    @Mapping(target = "urgency", ignore = true)
    StockAlertResponse toStockAlertResponse(Product product);

    @AfterMapping
    default void calculateStockAlertDetails(Product product, @MappingTarget StockAlertResponse response) {
        if (product.getReorderLevel() != null && product.getCurrentStock() != null) {
            BigDecimal deficit = product.getReorderLevel().subtract(product.getCurrentStock());
            response.setDeficit(deficit);

            String urgency;
            if (product.getCurrentStock().compareTo(BigDecimal.ZERO) == 0) {
                urgency = "OUT_OF_STOCK";
            } else if (deficit.compareTo(product.getReorderLevel().multiply(new BigDecimal("0.5"))) > 0) {
                urgency = "HIGH";
            } else if (deficit.compareTo(product.getReorderLevel().multiply(new BigDecimal("0.25"))) > 0) {
                urgency = "MEDIUM";
            } else {
                urgency = "LOW";
            }
            response.setUrgency(urgency);
        }
    }
}

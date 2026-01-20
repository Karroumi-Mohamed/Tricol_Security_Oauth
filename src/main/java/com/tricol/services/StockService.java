package com.tricol.services;

import com.tricol.dtos.response.ProductStockResponse;
import com.tricol.dtos.response.StockAlertResponse;
import com.tricol.dtos.response.StockMovementResponse;
import com.tricol.dtos.response.StockValuationResponse;
import com.tricol.entities.Order;
import com.tricol.entities.Product;

import java.math.BigDecimal;
import java.util.List;

public interface StockService {
    void createStockEntry(Product product, Order order, BigDecimal quantity, BigDecimal purchasePrice);

    void consumeStockFIFO(Product product, BigDecimal quantityNeeded, String reference);

    ProductStockResponse getProductStockDetails(Long productId);

    List<ProductStockResponse> getAllProductsStock();

    StockValuationResponse getStockValuation();

    List<StockAlertResponse> getStockAlerts();

    List<StockMovementResponse> getAllMovements();

    List<StockMovementResponse> getMovementsByProductId(Long productId);
}

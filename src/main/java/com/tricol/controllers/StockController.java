package com.tricol.controllers;

import com.tricol.dtos.response.ProductStockResponse;
import com.tricol.dtos.response.StockAlertResponse;
import com.tricol.dtos.response.StockMovementResponse;
import com.tricol.dtos.response.StockValuationResponse;
import com.tricol.services.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
@PreAuthorize("hasAuthority('VIEW_STOCK')")
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<ProductStockResponse>> getAllStock() {
        return ResponseEntity.ok(stockService.getAllProductsStock());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductStockResponse> getStockByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.getProductStockDetails(productId));
    }

    @GetMapping("/movements")
    @PreAuthorize("hasAuthority('VIEW_STOCK_MOVEMENTS')")
    public ResponseEntity<List<StockMovementResponse>> getAllStockMovements() {
        return ResponseEntity.ok(stockService.getAllMovements());
    }

    @GetMapping("/movements/product/{productId}")
    @PreAuthorize("hasAuthority('VIEW_STOCK_MOVEMENTS')")
    public ResponseEntity<List<StockMovementResponse>> getMovementsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.getMovementsByProductId(productId));
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyAuthority('CONFIGURE_STOCK_ALERTS', 'VIEW_STOCK')")
    public ResponseEntity<List<StockAlertResponse>> getStockAlerts() {
        return ResponseEntity.ok(stockService.getStockAlerts());
    }

    @GetMapping("/valuation")
    @PreAuthorize("hasAuthority('VIEW_STOCK_VALUATION')")
    public ResponseEntity<StockValuationResponse> getStockValuation() {
        return ResponseEntity.ok(stockService.getStockValuation());
    }
}

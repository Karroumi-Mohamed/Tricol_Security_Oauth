package com.tricol.services.impl;

import com.tricol.dtos.response.*;
import com.tricol.entities.Order;
import com.tricol.entities.Product;
import com.tricol.entities.StockLot;
import com.tricol.entities.StockMovement;
import com.tricol.entities.enums.MovementType;
import com.tricol.exceptions.InsufficientStockException;
import com.tricol.exceptions.ResourceNotFoundException;
import com.tricol.mappers.StockMapper;
import com.tricol.repositories.ProductRepository;
import com.tricol.repositories.StockLotRepository;
import com.tricol.repositories.StockMovementRepository;
import com.tricol.services.StockService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockLotRepository stockLotRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final StockMapper stockMapper;

    @Override
    @Transactional
    public void createStockEntry(Product product, Order order, BigDecimal quantity, BigDecimal purchasePrice) {
        StockLot lot = StockLot.builder()
                .lotNumber(generateLotNumber())
                .product(product)
                .order(order)
                .initialQuantity(quantity)
                .remainingQuantity(quantity)
                .purchasePrice(purchasePrice)
                .entryDate(LocalDateTime.now())
                .build();

        stockLotRepository.save(lot);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .stockLot(lot)
                .quantity(quantity)
                .movementType(MovementType.ENTRY)
                .movementDate(LocalDateTime.now())
                .reference("ORDER-" + order.getId())
                .build();

        stockMovementRepository.save(movement);

        product.setCurrentStock(product.getCurrentStock().add(quantity));
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void consumeStockFIFO(Product product, BigDecimal quantityNeeded, String reference) {
        List<StockLot> availableLots = stockLotRepository
                .findAvailableLotsByProductIdOrderByEntryDateAsc(product.getId());

        BigDecimal totalAvailable = availableLots.stream()
                .map(StockLot::getRemainingQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAvailable.compareTo(quantityNeeded) < 0) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName() +
                            ". Available: " + totalAvailable +
                            ", Requested: " + quantityNeeded);
        }

        BigDecimal remainingToConsume = quantityNeeded;

        for (StockLot lot : availableLots) {
            if (remainingToConsume.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal available = lot.getRemainingQuantity();
            BigDecimal toConsume;

            if (available.compareTo(remainingToConsume) >= 0) { // enough in this lot
                toConsume = remainingToConsume;
                remainingToConsume = BigDecimal.ZERO;
            } else { // consume all available and continue
                toConsume = available;
                remainingToConsume = remainingToConsume.subtract(available);
            }

            lot.setRemainingQuantity(lot.getRemainingQuantity().subtract(toConsume));
            stockLotRepository.save(lot);

            StockMovement movement = StockMovement.builder()
                    .product(product)
                    .stockLot(lot)
                    .quantity(toConsume)
                    .movementType(MovementType.EXIT)
                    .movementDate(LocalDateTime.now())
                    .reference(reference)
                    .build();

            stockMovementRepository.save(movement);
        }

        product.setCurrentStock(product.getCurrentStock().subtract(quantityNeeded));
        productRepository.save(product);
    }

    @Override
    public ProductStockResponse getProductStockDetails(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + productId + " not found."));

        List<StockLot> lots = stockLotRepository.findAvailableLotsByProductIdOrderByEntryDateAsc(productId);

        BigDecimal totalQuantity = lots.stream()
                .map(StockLot::getRemainingQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalValue = lots.stream()
                .map(lot -> lot.getRemainingQuantity().multiply(lot.getPurchasePrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean belowReorderLevel = product.getCurrentStock().compareTo(product.getReorderLevel()) <= 0;

        return ProductStockResponse.builder()
                .productId(product.getId())
                .productReference(product.getReference())
                .productName(product.getName())
                .unitOfMeasure(product.getUnitOfMeasure())
                .totalQuantity(totalQuantity)
                .totalValue(totalValue)
                .reorderLevel(product.getReorderLevel())
                .belowReorderLevel(belowReorderLevel)
                .lots(lots.stream().map(stockMapper::toStockLotResponse).toList())
                .build();
    }

    @Override
    public List<ProductStockResponse> getAllProductsStock() {
        return productRepository.findAll().stream()
                .map(product -> getProductStockDetails(product.getId()))
                .toList();
    }

    @Override
    public StockValuationResponse getStockValuation() {
        List<ProductStockResponse> products = getAllProductsStock();

        BigDecimal totalValue = products.stream()
                .map(ProductStockResponse::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalLots = products.stream()
                .mapToInt(p -> p.getLots().size())
                .sum();

        return StockValuationResponse.builder()
                .totalValue(totalValue)
                .totalProducts(products.size())
                .totalLots(totalLots)
                .calculatedAt(LocalDateTime.now())
                .products(products)
                .build();
    }

    @Override
    public List<StockAlertResponse> getStockAlerts() {
        return productRepository.findProductsBelowReorderLevel().stream()
                .map(stockMapper::toStockAlertResponse)
                .toList();
    }

    @Override
    public List<StockMovementResponse> getAllMovements() {
        return stockMovementRepository.findAll().stream()
                .map(stockMapper::toStockMovementResponse)
                .toList();
    }

    @Override
    public List<StockMovementResponse> getMovementsByProductId(Long productId) {
        return stockMovementRepository.findByProductIdOrderByMovementDateDesc(productId).stream()
                .map(stockMapper::toStockMovementResponse)
                .toList();
    }

    private String generateLotNumber() {
        return "LOT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

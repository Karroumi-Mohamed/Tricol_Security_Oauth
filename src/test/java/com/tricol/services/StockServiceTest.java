package com.tricol.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tricol.dtos.response.StockValuationResponse;
import com.tricol.entities.Order;
import com.tricol.entities.Product;
import com.tricol.entities.StockLot;
import com.tricol.entities.StockMovement;
import com.tricol.entities.enums.MovementType;
import com.tricol.exceptions.InsufficientStockException;
import com.tricol.repositories.ProductRepository;
import com.tricol.repositories.StockLotRepository;
import com.tricol.repositories.StockMovementRepository;

import com.tricol.services.impl.StockServiceImpl;
import com.tricol.mappers.StockMapper;
import com.tricol.dtos.response.StockLotResponse;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {
    @Mock
    private StockLotRepository stockLotRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMapper stockMapper;

    @InjectMocks
    private StockServiceImpl stockService;

    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .reference("TP-001")
                .currentStock(BigDecimal.ZERO)
                .reorderLevel(BigDecimal.TEN)
                .build();

        order = Order.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .build();
    }

    @Test
    void consumeStockFIFO_partialConsumption_singleLot() {
        BigDecimal quantityNeeded = new BigDecimal("20");

        StockLot lot = StockLot.builder()
                .id(1L)
                .remainingQuantity(new BigDecimal("100"))
                .product(product)
                .entryDate(LocalDateTime.now().minusDays(5))
                .build();

        when(stockLotRepository.findAvailableLotsByProductIdOrderByEntryDateAsc(product.getId()))
                .thenReturn(Collections.singletonList(lot));

        stockService.consumeStockFIFO(product, quantityNeeded, "REF-EX-001");

        assertEquals(new BigDecimal("80"), lot.getRemainingQuantity());
        verify(stockLotRepository).save(lot);
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void consumeStockFIFO_MultipleLotsConsumption() {
        BigDecimal quantityNeeded = new BigDecimal("80");

        StockLot lot1 = StockLot.builder()
                .id(1L)
                .remainingQuantity(new BigDecimal("50"))
                .product(product)
                .entryDate(LocalDateTime.now().minusDays(10))
                .build();

        StockLot lot2 = StockLot.builder()
                .id(1L)
                .remainingQuantity(new BigDecimal("50"))
                .product(product)
                .entryDate(LocalDateTime.now().minusDays(5))
                .build();

        when(stockLotRepository.findAvailableLotsByProductIdOrderByEntryDateAsc(product.getId()))
                .thenReturn(Arrays.asList(lot1, lot2));

        stockService.consumeStockFIFO(product, quantityNeeded, "REF-EX-002");

        assertEquals(BigDecimal.ZERO, lot1.getRemainingQuantity());

        assertEquals(new BigDecimal("20"), lot2.getRemainingQuantity());

        verify(stockLotRepository, times(2)).save(any(StockLot.class));
        verify(stockMovementRepository, times(2)).save(any(StockMovement.class));
    }

    @Test
    void consumeStockFIFO_InsuficientStock() {
        BigDecimal quantityNeeded = new BigDecimal("100");

        StockLot lot = StockLot.builder()
                .id(1L)
                .remainingQuantity(new BigDecimal("30"))
                .product(product)
                .build();

        when(stockLotRepository.findAvailableLotsByProductIdOrderByEntryDateAsc(product.getId()))
                .thenReturn(Collections.singletonList(lot));

        assertThrows(InsufficientStockException.class, () -> {
            stockService.consumeStockFIFO(product, quantityNeeded, "REF-EXIT-3");
        });

        verify(stockLotRepository, never()).save(any(StockLot.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void consumeStockFIFO_ExactLotCOnsumption() {
        BigDecimal quantityNeeded = new BigDecimal("100");

        StockLot lot = StockLot.builder()
                .id(1L)
                .remainingQuantity(new BigDecimal("100"))
                .product(product)
                .build();

        when(stockLotRepository.findAvailableLotsByProductIdOrderByEntryDateAsc(product.getId()))
                .thenReturn(Collections.singletonList(lot));

        stockService.consumeStockFIFO(product, quantityNeeded, "REF-EXIT-4");

        assertEquals(BigDecimal.ZERO, lot.getRemainingQuantity());

        verify(stockLotRepository).save(any(StockLot.class));
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void createStockEntry_ShouldCreateLotAndMovement() {
        BigDecimal quantity = new BigDecimal("50");
        BigDecimal price = new BigDecimal("10.50");

        stockService.createStockEntry(product, order, quantity, price);

        ArgumentCaptor<StockLot> lotCaptor = ArgumentCaptor.forClass(StockLot.class);
        verify(stockLotRepository).save(lotCaptor.capture());
        StockLot savedLot = lotCaptor.getValue();

        assertNotNull(savedLot.getLotNumber());
        assertEquals(quantity, savedLot.getInitialQuantity());
        assertEquals(quantity, savedLot.getRemainingQuantity());
        assertEquals(price, savedLot.getPurchasePrice());
        assertEquals(order, savedLot.getOrder());
        assertNotNull(savedLot.getEntryDate());

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());
        StockMovement savedMovement = movementCaptor.getValue();

        assertEquals(MovementType.ENTRY, savedMovement.getMovementType());
        assertEquals(quantity, savedMovement.getQuantity());

        assertEquals(quantity, product.getCurrentStock());
        verify(productRepository).save(product);
    }

    @Test
    void getStockValuation_ShouldReturnCorrectValuation() {
        StockLot lot1 = StockLot.builder()
                .id(1L)
                .purchasePrice(new BigDecimal("100"))
                .initialQuantity(new BigDecimal("10"))
                .remainingQuantity(new BigDecimal("10"))
                .product(product)
                .order(order)
                .build();
        StockLot lot2 = StockLot.builder()
                .id(2L)
                .purchasePrice(new BigDecimal("120"))
                .initialQuantity(new BigDecimal("5"))
                .remainingQuantity(new BigDecimal("5"))
                .product(product)
                .order(order)
                .build();

        when(productRepository.findAll()).thenReturn(Collections.singletonList(product));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(stockLotRepository.findAvailableLotsByProductIdOrderByEntryDateAsc(product.getId()))
                .thenReturn(Arrays.asList(lot1, lot2));

        when(stockMapper.toStockLotResponse(any(StockLot.class))).thenReturn(new StockLotResponse());

        StockValuationResponse response = stockService.getStockValuation();

        assertEquals(new BigDecimal("1600"), response.getTotalValue());
    }
}

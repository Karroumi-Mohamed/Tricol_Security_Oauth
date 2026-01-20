package com.tricol.repositories;

import com.tricol.entities.StockLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface StockLotRepository extends JpaRepository<StockLot, Long> {
    @Query("""
            SELECT sl FROM StockLot sl
            WHERE sl.product.id = :productId
            AND sl.remainingQuantity > 0
            ORDER BY sl.entryDate ASC
            """)
    List<StockLot> findAvailableLotsByProductIdOrderByEntryDateAsc(Long productId);

    List<StockLot> findByProductId(Long productId);

    List<StockLot> findByOrderId(Long orderId);

    boolean existsByLotNumber(String lotNumber);

    @Query("SELECT SUM(sl.remainingQuantity) FROM StockLot sl WHERE sl.product.id = :productId")
    BigDecimal getTotalRemainingQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(sl.purchasePrice * sl.remainingQuantity) FROM StockLot sl WHERE sl.product.id = :productId")
    BigDecimal getStockValueByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(sl.purchasePrice * sl.remainingQuantity) FROM StockLot sl")
    BigDecimal getTotalStockValue();

    @Query("SELECT sl FROM StockLot sl WHERE sl.remainingQuantity = 0")
    List<StockLot> findEmptyLots();
}

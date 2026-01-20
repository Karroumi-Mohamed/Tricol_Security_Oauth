package com.tricol.repositories;

import com.tricol.entities.StockMovement;
import com.tricol.entities.enums.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductIdOrderByMovementDateDesc(Long productId);

    List<StockMovement> findByStockLotId(Long stockLotId);

    List<StockMovement> findByMovementType(MovementType movementType);

    List<StockMovement> findByReference(String reference);

    List<StockMovement> findByMovementDateBetween(LocalDateTime movementDateAfter, LocalDateTime movementDateBefore);

    @Query("SELECT sm FROM StockMovement sm ORDER BY sm.movementDate DESC LIMIT :limit")
    List<StockMovement> findRecentMovements(@Param("limit") int limit);

    @Query("""
        SELECT SUM(sm.quantity) FROM StockMovement sm 
        WHERE sm.product.id = :productId AND sm.movementType = 'ENTRY'
    """)
    BigDecimal getTotalEntriesForProduct(@Param("productId") Long productId);

    @Query("""
        SELECT SUM(sm.quantity) FROM StockMovement sm 
        WHERE sm.product.id = :productId AND sm.movementType = 'EXIT'
    """)
    BigDecimal getTotalExitsForProduct(@Param("productId") Long productId);


}

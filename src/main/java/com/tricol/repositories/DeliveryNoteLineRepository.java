package com.tricol.repositories;

import com.tricol.entities.DeliveryNoteLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DeliveryNoteLineRepository extends JpaRepository<DeliveryNoteLine, Long> {
    List<DeliveryNoteLine> findByDeliveryNoteId(Long deliveryNoteId);

    List<DeliveryNoteLine> findByProductId(Long productId);

    @Query("SELECT SUM(dnl.quantity) FROM DeliveryNoteLine dnl WHERE dnl.product.id = :productId")
    BigDecimal getTotalRequestedForProduct(@Param("productId") Long productId);

    @Query("SELECT DISTINCT dnl.deliveryNote FROM DeliveryNoteLine dnl WHERE dnl.product.id = :productId")
    List<DeliveryNoteLine> findDeliveryNotesContainingProduct(@Param("productId") Long productId);
}

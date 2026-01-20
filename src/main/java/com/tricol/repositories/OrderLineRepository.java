package com.tricol.repositories;

import com.tricol.entities.OrderLine;
import com.tricol.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {
    List<OrderLine> findByOrderId(Long orderId);

    List<OrderLine> findByProduct(Product product);

    List<OrderLine> findByProductId(Long productId);

    @Query("SELECT SUM(ol.quantity) FROM OrderLine ol WHERE ol.product.id = :productId")
    BigDecimal sumQuantityByProductId(@Param("productId") Long productId);
}

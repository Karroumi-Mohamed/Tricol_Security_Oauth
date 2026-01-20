package com.tricol.repositories;

import com.tricol.entities.Order;
import com.tricol.entities.Supplier;
import com.tricol.entities.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findBySupplier(Supplier supplier);

    List<Order> findBySupplierId(Long supplierId);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    List<Order> findByOrderDateBetween(LocalDate orderDateAfter, LocalDate orderDateBefore);

    List<Order> findBySupplierIdAndOrderStatus(Long supplierId, OrderStatus orderStatus);

    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'PENDING' ORDER BY o.orderDate ASC")
    List<Order> findPendingOrders();

    boolean existsByOrderNumber(String orderNumber);

    List<Order> findOrdersByTotalAmountBetween(BigDecimal min, BigDecimal max);
}

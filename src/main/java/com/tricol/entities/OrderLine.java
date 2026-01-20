package com.tricol.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_line")
public class OrderLine {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

   @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal quantity;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal subTotal;

    @PrePersist
    @PreUpdate
    private void calculateSubTotal() {
        if (quantity != null && unitPrice != null) {
            this.subTotal = unitPrice.multiply(quantity);
        } else {
            this.subTotal = BigDecimal.ZERO;
        }
    }

    public BigDecimal getSubTotal() {
        if (subTotal == null) {
            if (quantity != null && unitPrice != null) {
                return unitPrice.multiply(quantity);
            }
            return BigDecimal.ZERO;
        }
        return subTotal;
    }
}

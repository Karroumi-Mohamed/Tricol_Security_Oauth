package com.tricol.services;

import com.tricol.dtos.request.OrderRequest;
import com.tricol.dtos.response.OrderResponse;
import com.tricol.entities.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse create(OrderRequest request);

    List<OrderResponse> findAll();

    OrderResponse findById(Long id);

    List<OrderResponse> findBySupplierId(Long supplierId);

    List<OrderResponse> findByStatus(OrderStatus orderStatus);

    OrderResponse update(Long id, OrderRequest request);

    void delete(Long id);

    OrderResponse receiveOrder(Long id);

    OrderResponse cancelOrder(Long id);
}

package com.tricol.services.impl;

import com.tricol.dtos.request.OrderLineRequest;
import com.tricol.dtos.request.OrderRequest;
import com.tricol.dtos.response.OrderResponse;
import com.tricol.entities.Order;
import com.tricol.entities.OrderLine;
import com.tricol.entities.Product;
import com.tricol.entities.Supplier;
import com.tricol.entities.enums.OrderStatus;
import com.tricol.exceptions.InvalidOperationException;
import com.tricol.exceptions.ResourceNotFoundException;
import com.tricol.mappers.OrderMapper;
import com.tricol.repositories.OrderRepository;
import com.tricol.repositories.ProductRepository;
import com.tricol.repositories.SupplierRepository;
import com.tricol.services.OrderService;
import com.tricol.services.StockService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse create(OrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .supplier(supplier)
                .orderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now())
                .orderStatus(OrderStatus.PENDING)
                .build();

        for (OrderLineRequest lineRequest : request.getLines()) {
            Product product = productRepository.findById(lineRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + lineRequest.getProductId()));

            OrderLine line = OrderLine.builder()
                    .product(product)
                    .quantity(lineRequest.getQuantity())
                    .unitPrice(lineRequest.getUnitPrice())
                    .build();

            order.addLine(line);
        }

        order.calculateTotalAmount();

        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    @Override
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        return orderMapper.toResponse(order);
    }

    @Override
    public List<OrderResponse> findBySupplierId(Long supplierId) {
        return orderRepository.findBySupplierId(supplierId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> findByStatus(OrderStatus orderStatus) {
        return orderRepository.findByOrderStatus(orderStatus).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse update(Long id, OrderRequest request) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new InvalidOperationException("Cannot modify order with status: " + order.getOrderStatus());
        }

        if (!request.getSupplierId().equals(order.getSupplier().getId())) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Supplier not found with id: " + request.getSupplierId()));
            order.setSupplier(supplier);
        }

        order.getLines().clear();

        for (OrderLineRequest lineRequest : request.getLines()) {
            Product product = productRepository.findById(lineRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + lineRequest.getProductId()));

            OrderLine line = OrderLine.builder()
                    .product(product)
                    .quantity(lineRequest.getQuantity())
                    .unitPrice(lineRequest.getUnitPrice())
                    .build();

            order.addLine(line);
        }

        order.calculateTotalAmount();

        Order updated = orderRepository.save(order);
        return orderMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new InvalidOperationException("Cannot delete order with status: " + order.getOrderStatus());
        }

        orderRepository.delete(order);
    }

    @Override
    @Transactional
    public OrderResponse receiveOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOperationException("Order already received");
        }

        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new InvalidOperationException("Cannot receive cancelled order");
        }

        for (OrderLine line : order.getLines()) {
            stockService.createStockEntry(
                    line.getProduct(),
                    order,
                    line.getQuantity(),
                    line.getUnitPrice());
        }

        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setDeliveryDate(LocalDate.now());

        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOperationException("Cannot cancel delivered order");
        }

        order.setOrderStatus(OrderStatus.CANCELED);

        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

package com.tricol.controllers;

import com.tricol.dtos.request.OrderRequest;
import com.tricol.dtos.response.OrderResponse;
import com.tricol.entities.enums.OrderStatus;
import com.tricol.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ORDER')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ORDER')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_ORDER')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("hasAuthority('VIEW_ORDER')")
    public ResponseEntity<List<OrderResponse>> getOrdersBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(orderService.findBySupplierId(supplierId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('VIEW_ORDER')")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(orderService.findByStatus(orderStatus));
    }

    @PutMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('RECEIVE_ORDER')")
    public ResponseEntity<OrderResponse> receiveOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.receiveOrder(id));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('CANCEL_ORDER')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}

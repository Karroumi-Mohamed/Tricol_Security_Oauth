package com.tricol.controllers;

import com.tricol.dtos.request.SupplierRequest;
import com.tricol.dtos.response.SupplierResponse;
import com.tricol.services.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;


    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_SUPPLIER')")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_SUPPLIER')")
    public ResponseEntity<List<SupplierResponse>> findAll() {
        return ResponseEntity.ok(supplierService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_SUPPLIER')")
    public ResponseEntity<SupplierResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_SUPPLIER')")
    public ResponseEntity<SupplierResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request){
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('VIEW_SUPPLIER')")
    public ResponseEntity<List<SupplierResponse>> searchByName(
           @RequestParam String name) {
        return ResponseEntity.ok(supplierService.searchByName(name));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_SUPPLIER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.tricol.services;

import com.tricol.dtos.request.SupplierRequest;
import com.tricol.dtos.response.SupplierResponse;

import java.util.List;

public interface SupplierService {
    SupplierResponse create(SupplierRequest request);

    SupplierResponse findById(Long id);

    List<SupplierResponse> searchByName(String name);

    List<SupplierResponse> findAll();

    SupplierResponse update(Long id, SupplierRequest request);

    void delete(Long id);
}

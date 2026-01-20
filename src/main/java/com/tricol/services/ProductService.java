package com.tricol.services;

import com.tricol.dtos.request.ProductRequest;
import com.tricol.dtos.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest request);

    List<ProductResponse> findAll();

    ProductResponse findById(Long id);

    List<ProductResponse> findByCategory(String category);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);

    List<ProductResponse> searchByName(String name);
}

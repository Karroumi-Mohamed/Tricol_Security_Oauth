package com.tricol.services.impl;

import com.tricol.dtos.request.ProductRequest;
import com.tricol.dtos.response.ProductResponse;
import com.tricol.entities.Product;
import com.tricol.exceptions.DuplicateResourceException;
import com.tricol.exceptions.ResourceNotFoundException;
import com.tricol.mappers.ProductMapper;
import com.tricol.repositories.ProductRepository;
import com.tricol.services.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsByReference(request.getReference())) {
            throw new DuplicateResourceException("A product with the same reference already exists.");
        }

        Product product = productMapper.toEntity(request);
        // Default values if not set by mapper (though builder usually handles nulls, mapstruct creates new obj)
        if (product.getCurrentStock() == null) {
            product.setCurrentStock(BigDecimal.ZERO);
        }

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found."));

        return productMapper.toResponse(product);
    }

    @Override
    public List<ProductResponse> findByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found."));

        if (!request.getReference().equals(product.getReference())) {
            if (productRepository.existsByReference(request.getReference())) {
                throw new DuplicateResourceException(
                        "A product with the reference " + request.getReference() + " already exists.");
            }
        }

        productMapper.updateEntityFromRequest(request, product);

        Product updated = productRepository.save(product);
        return productMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found."));
        if (product.getCurrentStock().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Cannot delete product with id " + id + " because it has stock available.");
        }

        productRepository.deleteById(id);
    }

    @Override
    public List<ProductResponse> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(productMapper::toResponse)
                .toList();
    }
}

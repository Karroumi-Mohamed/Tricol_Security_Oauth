package com.tricol.services.impl;

import com.tricol.dtos.request.SupplierRequest;
import com.tricol.dtos.response.SupplierResponse;
import com.tricol.entities.Supplier;
import com.tricol.exceptions.DuplicateResourceException;
import com.tricol.exceptions.ResourceNotFoundException;
import com.tricol.mappers.SupplierMapper;
import com.tricol.repositories.SupplierRepository;
import com.tricol.services.AuditLogService;
import com.tricol.services.SupplierService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        if (request.getIce() != null && supplierRepository.existsByIce(request.getIce())) {
            throw new DuplicateResourceException("A supplier with the same ICE already exists.");
        }

        Supplier supplier = supplierMapper.toEntity(request);
        Supplier saved = supplierRepository.save(supplier);
        auditLogService.log(null, "CREATE_SUPPLIER", "Supplier", saved.getId(),
                "Supplier " + saved.getCompanyName() + " created");
        return supplierMapper.toResponse(saved);
    }

    @Override
    public SupplierResponse findById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with id " + id + " not found."));

        return supplierMapper.toResponse(supplier);
    }

    @Override
    public List<SupplierResponse> searchByName(String name) {
        return supplierRepository.findByCompanyNameContainingIgnoreCase(name).stream()
                .map(supplierMapper::toResponse)
                .toList();
    }

    @Override
    public List<SupplierResponse> findAll() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with id " + id + " not found."));

        if (request.getIce() != null && !request.getIce().equals(supplier.getIce())) {
            if (supplierRepository.existsByIce(request.getIce())) {
                throw new DuplicateResourceException(
                        "A supplier with the ICE " + request.getIce() + " already exists.");
            }
        }

        supplierMapper.updateEntityFromRequest(request, supplier);

        Supplier updated = supplierRepository.save(supplier);
        auditLogService.log(null, "UPDATE_SUPPLIER", "Supplier", updated.getId(),
                "Supplier " + updated.getCompanyName() + " updated");
        return supplierMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier with id " + id + " not found.");
        }
        supplierRepository.deleteById(id);
        auditLogService.log(null, "DELETE_SUPPLIER", "Supplier", id, "Supplier deleted");
    }
}

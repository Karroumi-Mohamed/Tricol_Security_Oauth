package com.tricol.mappers;

import com.tricol.dtos.request.SupplierRequest;
import com.tricol.dtos.response.SupplierResponse;
import com.tricol.entities.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    Supplier toEntity(SupplierRequest request);

    SupplierResponse toResponse(Supplier supplier);

    void updateEntityFromRequest(SupplierRequest request, @MappingTarget Supplier supplier);
}

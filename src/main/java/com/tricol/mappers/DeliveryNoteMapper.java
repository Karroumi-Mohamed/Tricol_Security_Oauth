package com.tricol.mappers;

import com.tricol.dtos.response.DeliveryNoteLineResponse;
import com.tricol.dtos.response.DeliveryNoteResponse;
import com.tricol.entities.DeliveryNote;
import com.tricol.entities.DeliveryNoteLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeliveryNoteMapper {

    @Mapping(target = "lines", source = "lines")
    @Mapping(target = "status", source = "status")
    DeliveryNoteResponse toResponse(DeliveryNote note);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productReference", source = "product.reference")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "unitOfMeasure", source = "product.unitOfMeasure")
    DeliveryNoteLineResponse toLineResponse(DeliveryNoteLine line);
}

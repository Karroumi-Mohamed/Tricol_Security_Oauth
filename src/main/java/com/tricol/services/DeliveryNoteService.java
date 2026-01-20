package com.tricol.services;

import com.tricol.dtos.request.DeliveryNoteRequest;
import com.tricol.dtos.response.DeliveryNoteResponse;
import com.tricol.entities.enums.DeliveryNoteStatus;

import java.util.List;

public interface DeliveryNoteService {
    DeliveryNoteResponse create(DeliveryNoteRequest request);

    List<DeliveryNoteResponse> findAll();

    DeliveryNoteResponse findById(Long id);

    List<DeliveryNoteResponse> findByWorkshop(String workshop);

    List<DeliveryNoteResponse> findByStatus(DeliveryNoteStatus status);

    DeliveryNoteResponse update(Long id, DeliveryNoteRequest request);

    DeliveryNoteResponse validate(Long id);

    DeliveryNoteResponse cancel(Long id);

    void delete(Long id);
}

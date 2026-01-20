package com.tricol.services.impl;

import com.tricol.dtos.request.DeliveryNoteLineRequest;
import com.tricol.dtos.request.DeliveryNoteRequest;
import com.tricol.dtos.response.DeliveryNoteResponse;
import com.tricol.entities.DeliveryNote;
import com.tricol.entities.DeliveryNoteLine;
import com.tricol.entities.Product;
import com.tricol.entities.enums.DeliveryNoteStatus;
import com.tricol.entities.enums.ExitReason;
import com.tricol.exceptions.InvalidOperationException;
import com.tricol.exceptions.ResourceNotFoundException;
import com.tricol.mappers.DeliveryNoteMapper;
import com.tricol.repositories.DeliveryNoteRepository;
import com.tricol.repositories.ProductRepository;
import com.tricol.services.DeliveryNoteService;
import com.tricol.services.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryNoteServiceImpl implements DeliveryNoteService {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final DeliveryNoteMapper deliveryNoteMapper;

    @Override
    @Transactional
    public DeliveryNoteResponse create(DeliveryNoteRequest request) {

        DeliveryNote note = DeliveryNote.builder()
                .deliveryNoteNumber(generateNoteNumber())
                .workshop(request.getWorkshop())
                .exitDate(request.getExitDate() != null ? request.getExitDate() : LocalDate.now())
                .exitReason(request.getExitReason() != null
                        ? ExitReason.valueOf(request.getExitReason())
                        : ExitReason.PRODUCTION)
                .status(DeliveryNoteStatus.DRAFT)
                .build();

        for (DeliveryNoteLineRequest lineRequest : request.getLines()) {
            Product product = productRepository.findById(lineRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + lineRequest.getProductId()));

            DeliveryNoteLine line = DeliveryNoteLine.builder()
                    .product(product)
                    .quantity(lineRequest.getQuantity())
                    .build();

            note.addLine(line);
        }

        DeliveryNote saved = deliveryNoteRepository.save(note);
        return deliveryNoteMapper.toResponse(saved);
    }

    @Override
    public List<DeliveryNoteResponse> findAll() {
        return deliveryNoteRepository.findAll().stream()
                .map(deliveryNoteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryNoteResponse findById(Long id) {
        DeliveryNote note = deliveryNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery note not found with id: " + id));
        return deliveryNoteMapper.toResponse(note);
    }

    @Override
    public List<DeliveryNoteResponse> findByWorkshop(String workshop) {
        return deliveryNoteRepository.findByWorkshop(workshop).stream()
                .map(deliveryNoteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryNoteResponse> findByStatus(DeliveryNoteStatus status) {
        return deliveryNoteRepository.findByStatus(status).stream()
                .map(deliveryNoteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeliveryNoteResponse update(Long id, DeliveryNoteRequest request) {
        DeliveryNote note = deliveryNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery note not found with id: " + id));

        if (note.getStatus() != DeliveryNoteStatus.DRAFT) {
            throw new InvalidOperationException("Cannot modify delivery note with status: " + note.getStatus());
        }

        note.setWorkshop(request.getWorkshop());
        note.setExitDate(request.getExitDate() != null ? request.getExitDate() : note.getExitDate());
        if (request.getExitReason() != null) {
            note.setExitReason(ExitReason.valueOf(request.getExitReason()));
        }

        note.getLines().clear();

        for (DeliveryNoteLineRequest lineRequest : request.getLines()) {
            Product product = productRepository.findById(lineRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + lineRequest.getProductId()));

            DeliveryNoteLine line = DeliveryNoteLine.builder()
                    .product(product)
                    .quantity(lineRequest.getQuantity())
                    .build();

            note.addLine(line);
        }

        DeliveryNote saved = deliveryNoteRepository.save(note);
        return deliveryNoteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DeliveryNoteResponse validate(Long id) {
        DeliveryNote note = deliveryNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery note not found with id: " + id));

        if (note.getStatus() != DeliveryNoteStatus.DRAFT) {
            throw new InvalidOperationException(
                    "Can only validate DRAFT delivery notes. Current status: " + note.getStatus());
        }

        if (note.getLines().isEmpty()) {
            throw new InvalidOperationException("Cannot validate empty delivery note");
        }

        String reference = "DELIVERY-" + note.getId();

        for (DeliveryNoteLine line : note.getLines()) {
            stockService.consumeStockFIFO(
                    line.getProduct(),
                    line.getQuantity(),
                    reference);
        }

        note.setStatus(DeliveryNoteStatus.VALIDATED);
        note.setValidatedAt(LocalDateTime.now());

        DeliveryNote saved = deliveryNoteRepository.save(note);
        return deliveryNoteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DeliveryNoteResponse cancel(Long id) {
        DeliveryNote note = deliveryNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery note not found with id: " + id));

        if (note.getStatus() == DeliveryNoteStatus.VALIDATED) {
            throw new InvalidOperationException("Cannot cancel validated delivery note");
        }

        note.setStatus(DeliveryNoteStatus.CANCELLED);

        DeliveryNote saved = deliveryNoteRepository.save(note);
        return deliveryNoteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        DeliveryNote note = deliveryNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery note not found with id: " + id));

        if (note.getStatus() != DeliveryNoteStatus.DRAFT) {
            throw new InvalidOperationException("Cannot delete delivery note with status: " + note.getStatus());
        }

        deliveryNoteRepository.delete(note);
    }

    private String generateNoteNumber() {
        return "DN-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

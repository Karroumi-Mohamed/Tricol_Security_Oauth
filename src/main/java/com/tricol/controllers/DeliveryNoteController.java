package com.tricol.controllers;

import com.tricol.dtos.request.DeliveryNoteRequest;
import com.tricol.dtos.response.DeliveryNoteResponse;
import com.tricol.entities.enums.DeliveryNoteStatus;
import com.tricol.services.DeliveryNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery-notes")
@RequiredArgsConstructor
public class DeliveryNoteController {

    private final DeliveryNoteService deliveryNoteService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_DELIVERY_NOTE')")
    public ResponseEntity<DeliveryNoteResponse> create(@Valid @RequestBody DeliveryNoteRequest request) {
        DeliveryNoteResponse response = deliveryNoteService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_DELIVERY_NOTE')")
    public ResponseEntity<List<DeliveryNoteResponse>> findAll() {
        return ResponseEntity.ok(deliveryNoteService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_DELIVERY_NOTE')")
    public ResponseEntity<DeliveryNoteResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryNoteService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_DELIVERY_NOTE')")
    public ResponseEntity<DeliveryNoteResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryNoteRequest request) {
        return ResponseEntity.ok(deliveryNoteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_DELIVERY_NOTE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deliveryNoteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/workshop/{workshop}")
    @PreAuthorize("hasAuthority('VIEW_DELIVERY_NOTE')")
    public ResponseEntity<List<DeliveryNoteResponse>> findByWorkshop(@PathVariable String workshop) {
        return ResponseEntity.ok(deliveryNoteService.findByWorkshop(workshop));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('VIEW_DELIVERY_NOTE')")
    public ResponseEntity<List<DeliveryNoteResponse>> findByStatus(@PathVariable String status) {
        DeliveryNoteStatus noteStatus = DeliveryNoteStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(deliveryNoteService.findByStatus(noteStatus));
    }

    @PutMapping("/{id}/validate")
    @PreAuthorize("hasAuthority('VALIDATE_DELIVERY_NOTE')")
    public ResponseEntity<DeliveryNoteResponse> validate(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryNoteService.validate(id));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('CANCEL_DELIVERY_NOTE')")
    public ResponseEntity<DeliveryNoteResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryNoteService.cancel(id));
    }
}

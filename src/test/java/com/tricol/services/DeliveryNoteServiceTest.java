package com.tricol.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tricol.dtos.response.DeliveryNoteResponse;
import com.tricol.entities.DeliveryNote;
import com.tricol.entities.DeliveryNoteLine;
import com.tricol.entities.Product;
import com.tricol.entities.enums.DeliveryNoteStatus;
import com.tricol.entities.enums.ExitReason;
import com.tricol.repositories.DeliveryNoteRepository;
import com.tricol.repositories.ProductRepository;

import com.tricol.services.impl.DeliveryNoteServiceImpl;
import com.tricol.mappers.DeliveryNoteMapper;

@ExtendWith(MockitoExtension.class)
public class DeliveryNoteServiceTest {
    @Mock
    private DeliveryNoteRepository deliveryNoteRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockService stockService;

    @Mock
    private DeliveryNoteMapper deliveryNoteMapper;

    @InjectMocks
    private DeliveryNoteServiceImpl deliveryNoteService;

    @Test
    void validate_ShouldChnageStatusAndTrigggerStockConsumption() {
        Long noteId = 1L;
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .reference("TP-001")
                .build();

        DeliveryNoteLine line = DeliveryNoteLine.builder()
                .product(product)
                .quantity(new BigDecimal("10"))
                .build();

        DeliveryNote note = DeliveryNote.builder()
                .id(noteId)
                .deliveryNoteNumber("DN-001")
                .status(DeliveryNoteStatus.DRAFT)
                .exitDate(LocalDate.now())
                .exitReason(ExitReason.PRODUCTION)
                .lines(new ArrayList<>())
                .build();

        note.getLines().add(line);

        when(deliveryNoteRepository.findById(noteId)).thenReturn(Optional.of(note));
        when(deliveryNoteRepository.save(any(DeliveryNote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryNoteResponse mockResponse = DeliveryNoteResponse.builder()
                .status(DeliveryNoteStatus.VALIDATED.name())
                .validatedAt(LocalDate.now().atStartOfDay())
                .build();
        when(deliveryNoteMapper.toResponse(any(DeliveryNote.class))).thenReturn(mockResponse);

        DeliveryNoteResponse response = deliveryNoteService.validate(noteId);

        assertEquals("VALIDATED", response.getStatus());

        assertNotNull(response.getValidatedAt());

        verify(stockService).consumeStockFIFO(
                eq(product),
                eq(new BigDecimal("10")),
                eq("DELIVERY-" + noteId));

        verify(deliveryNoteRepository).save(note);
    }
}

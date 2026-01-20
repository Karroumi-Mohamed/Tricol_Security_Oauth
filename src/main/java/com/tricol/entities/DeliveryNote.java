package com.tricol.entities;

import com.tricol.entities.enums.DeliveryNoteStatus;
import com.tricol.entities.enums.ExitReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "delivery_note")
@Builder
public class DeliveryNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String deliveryNoteNumber;

    @Column(nullable = false)
    private String workshop;

    @Column(nullable = false)
    private LocalDate exitDate;


    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ExitReason exitReason = ExitReason.PRODUCTION;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DeliveryNoteStatus status = DeliveryNoteStatus.DRAFT;

    @OneToMany(mappedBy = "deliveryNote", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeliveryNoteLine> lines = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Helper methods
    public void addLine(DeliveryNoteLine line) {
        lines.add(line);
        line.setDeliveryNote(this);
    }

    public void removeLine(DeliveryNoteLine line) {
        lines.remove(line);
        line.setDeliveryNote(null);
    }
}

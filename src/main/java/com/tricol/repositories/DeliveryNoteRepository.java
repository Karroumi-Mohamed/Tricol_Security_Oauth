package com.tricol.repositories;

import com.tricol.entities.DeliveryNote;
import com.tricol.entities.enums.DeliveryNoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryNoteRepository extends JpaRepository<DeliveryNote, Long> {
    Optional<DeliveryNote> findByDeliveryNoteNumber(String deliveryNoteNumber);

    List<DeliveryNote> findByWorkshop(String workshop);

    List<DeliveryNote> findByStatus(DeliveryNoteStatus status);

    List<DeliveryNote> findByExitDateBetween(LocalDate exitDateAfter, LocalDate exitDateBefore);

    List<DeliveryNote> findByWorkshopAndStatus(String workshop, DeliveryNoteStatus status);

    boolean existsByDeliveryNoteNumber(String deliveryNoteNumber);

}

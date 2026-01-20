package com.tricol.repositories;

import com.tricol.entities.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByIce(String ice);

    List<Supplier> findByCity(String city);

    List<Supplier> findByCompanyNameContainingIgnoreCase(String companyName);

    boolean existsByIce(String ice);
}

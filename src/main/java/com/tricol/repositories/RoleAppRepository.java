package com.tricol.repositories;

import com.tricol.entities.RoleApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleAppRepository extends JpaRepository<RoleApp, Long> {
    Optional<RoleApp> findByName(String name);

}

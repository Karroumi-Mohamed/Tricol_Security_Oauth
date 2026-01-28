package com.tricol.repositories;

import com.tricol.entities.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAppRepository extends JpaRepository<UserApp, Long> {
    Optional<UserApp> findByUsername(String username);

    Optional<UserApp> findByKeyCloakId(String keyCloakId);

    boolean existsByUsername(String username);
}

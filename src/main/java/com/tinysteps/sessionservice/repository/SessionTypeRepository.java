package com.tinysteps.sessionservice.repository;

import com.tinysteps.sessionservice.entity.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface SessionTypeRepository
        extends JpaRepository<SessionType, UUID>,
        JpaSpecificationExecutor<SessionType> {

    Optional<SessionType> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}

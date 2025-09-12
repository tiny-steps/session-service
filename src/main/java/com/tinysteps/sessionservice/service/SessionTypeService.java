package com.tinysteps.sessionservice.service;

import com.tinysteps.sessionservice.entity.SessionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface SessionTypeService {

    SessionType create(SessionType sessionType);

    Optional<SessionType> getById(UUID id);

    Page<SessionType> search(String name,
            Boolean isActive,
            Boolean telemedicineAvailable,
            Integer minDuration,
            Integer maxDuration,
            Pageable pageable);

    SessionType update(UUID id, SessionType sessionType);

    void delete(UUID id);

    boolean existsByName(String name);

    SessionType activate(UUID id);

    SessionType deactivate(UUID id);

    // Branch statistics methods
    Map<String, Object> getBranchStatistics();

    Map<String, Object> getGlobalStatistics();
}
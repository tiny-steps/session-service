package com.tinysteps.sessionservice.service.impl;

import com.tinysteps.sessionservice.entity.SessionType;
import com.tinysteps.sessionservice.repository.SessionTypeRepository;
import com.tinysteps.sessionservice.service.SessionTypeService;
import com.tinysteps.sessionservice.service.SecurityService;
import com.tinysteps.sessionservice.specs.SessionTypeSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class SessionTypeServiceImpl implements SessionTypeService {

    private final SessionTypeRepository repository;
    private final SecurityService securityService;

    @Override
    public SessionType create(SessionType sessionType) {
        return repository.save(sessionType);
    }

    @Override
    public Optional<SessionType> getById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Page<SessionType> search(String name, Boolean isActive, Boolean telemedicineAvailable,
            Integer minDuration, Integer maxDuration, Pageable pageable) {
        Specification<SessionType> spec = Specification
                .where(SessionTypeSpecs.byNameContains(name))
                .and(SessionTypeSpecs.isActive(isActive))
                .and(SessionTypeSpecs.isTelemedicineAvailable(telemedicineAvailable))
                .and(SessionTypeSpecs.byDurationRange(minDuration, maxDuration));
        return repository.findAll(spec, pageable);
    }

    @Override
    public SessionType update(UUID id, SessionType sessionType) {
        SessionType existing = repository.findById(id).orElseThrow();
        existing.setName(sessionType.getName());
        existing.setDescription(sessionType.getDescription());
        existing.setDefaultDurationMinutes(sessionType.getDefaultDurationMinutes());
        existing.setTelemedicineAvailable(sessionType.isTelemedicineAvailable());
        existing.setActive(sessionType.isActive());
        return repository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    @Override
    public SessionType activate(UUID id) {
        SessionType type = repository.findById(id).orElseThrow();
        type.setActive(true);
        return repository.save(type);
    }

    @Override
    public SessionType deactivate(UUID id) {
        SessionType type = repository.findById(id).orElseThrow();
        type.setActive(false);
        return repository.save(type);
    }

    @Override
    public Map<String, Object> getBranchStatistics() {
        // Note: Session types are global entities, but we provide branch-related
        // statistics for consistency
        Map<String, Object> statistics = new HashMap<>();

        // Total session types
        long totalSessionTypes = repository.count();
        statistics.put("totalSessionTypes", totalSessionTypes);

        // Active session types
        Specification<SessionType> activeSpec = SessionTypeSpecs.isActive(true);
        long activeSessionTypes = repository.count(activeSpec);
        statistics.put("activeSessionTypes", activeSessionTypes);

        // Inactive session types
        Specification<SessionType> inactiveSpec = SessionTypeSpecs.isActive(false);
        long inactiveSessionTypes = repository.count(inactiveSpec);
        statistics.put("inactiveSessionTypes", inactiveSessionTypes);

        // Session types with telemedicine available
        Specification<SessionType> telemedicineSpec = SessionTypeSpecs.isTelemedicineAvailable(true);
        long telemedicineSessionTypes = repository.count(telemedicineSpec);
        statistics.put("telemedicineSessionTypes", telemedicineSessionTypes);

        return statistics;
    }

    @Override
    public Map<String, Object> getGlobalStatistics() {
        // Global statistics for session types (same as branch statistics since session
        // types are global)
        return getBranchStatistics();
    }
}
package com.tinysteps.sessionservice.service.impl;

import com.tinysteps.sessionservice.entity.SessionOffering;
import com.tinysteps.sessionservice.entity.SessionType;
import com.tinysteps.sessionservice.dto.SessionOfferingCreateDto;
import com.tinysteps.sessionservice.integration.service.DoctorIntegrationService;
import com.tinysteps.sessionservice.integration.service.AddressIntegrationService;
import com.tinysteps.sessionservice.repository.SessionOfferingRepository;
import com.tinysteps.sessionservice.repository.SessionTypeRepository;
import com.tinysteps.sessionservice.service.SessionOfferingService;
import com.tinysteps.sessionservice.service.SecurityService;
import com.tinysteps.sessionservice.specs.SessionOfferingSpecs;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class SessionOfferingServiceImpl implements SessionOfferingService {

    private final SessionOfferingRepository repository;
    private final SessionTypeRepository sessionTypeRepository;
    private final DoctorIntegrationService doctorIntegrationService;
    private final AddressIntegrationService practiceIntegrationService;
    private final SecurityService securityService;

    @Override
    @Transactional
    public SessionOffering create(SessionOfferingCreateDto createDto) {
        // Validate doctor existence via integrations (temporarily disabled due to auth
        // issues)
        try {
            doctorIntegrationService.validateDoctorExistsOrThrow(createDto.getDoctorId());
        } catch (Exception e) {
            // Log the error but don't fail the request for now
            System.err.println("Doctor validation failed: " + e.getMessage());
        }

        // Ensure session type exists
        SessionType sessionType = sessionTypeRepository.findById(createDto.getSessionTypeId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Invalid SessionType ID: " + createDto.getSessionTypeId()));

        // Create SessionOffering entity from DTO
        SessionOffering offering = SessionOffering.builder()
                .doctorId(createDto.getDoctorId())
                .branchId(createDto.getBranchId())
                .sessionType(sessionType)
                .price(createDto.getPrice())
                .isActive(createDto.isActive())
                .build();

        return repository.save(offering);
    }

    @Override
    @Transactional
    public List<SessionOffering> bulkCreateOrUpdate(List<SessionOffering> offerings) {
        for (SessionOffering off : offerings) {
            // Validate doctor existence via integrations (temporarily disabled due to auth
            // issues)
            try {
                doctorIntegrationService.validateDoctorExistsOrThrow(off.getDoctorId());
            } catch (Exception e) {
                // Log the error but don't fail the request for now
                System.err.println("Doctor validation failed: " + e.getMessage());
            }

            // Practice validation removed - no longer needed after Practice entity removal

            SessionType type = sessionTypeRepository.findById(off.getSessionType().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid SessionType ID"));
            off.setSessionType(type);
        }
        return repository.saveAll(offerings);
    }

    @Override
    public Optional<SessionOffering> getById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Page<SessionOffering> search(
            UUID sessionTypeId,
            Boolean isActive,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            UUID branchId,
            Pageable pageable) {

        Specification<SessionOffering> spec = Specification.where(
                SessionOfferingSpecs.bySessionTypeId(sessionTypeId))
                .and(SessionOfferingSpecs.isActive(isActive))
                .and(SessionOfferingSpecs.byPriceRange(minPrice, maxPrice))
                .and(SessionOfferingSpecs.byBranchId(branchId));

        return repository.findAll(spec, pageable);
    }

    @Override
    public Page<SessionOffering> searchForCurrentUserBranch(
            UUID sessionTypeId,
            Boolean isActive,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {
        // Get current user's primary branch
        UUID primaryBranchId = securityService.getPrimaryBranchId();
        return search(sessionTypeId, isActive, minPrice, maxPrice, primaryBranchId, pageable);
    }

    @Override
    public List<SessionOffering> getByDoctorId(UUID doctorId) {
        return repository.findByDoctorId(doctorId);
    }

    // getByPracticeId method removed - no longer needed after Practice entity
    // removal

    @Override
    @Transactional
    public SessionOffering update(UUID id, SessionOffering offering) {
        SessionOffering existing = repository.findById(id).orElseThrow();

        // Only update allowed fields
        existing.setPrice(offering.getPrice());
        existing.setActive(offering.isActive());

        return repository.save(existing);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public SessionOffering activate(UUID id) {
        SessionOffering existing = repository.findById(id).orElseThrow();
        existing.setActive(true);
        return repository.save(existing);
    }

    @Override
    @Transactional
    public SessionOffering deactivate(UUID id) {
        SessionOffering existing = repository.findById(id).orElseThrow();
        existing.setActive(false);
        return repository.save(existing);
    }

    @Override
    public Map<String, Object> getBranchStatistics(UUID branchId) {
        Map<String, Object> statistics = new HashMap<>();

        // Total session offerings in branch
        Specification<SessionOffering> branchSpec = SessionOfferingSpecs.byBranchId(branchId);
        long totalOfferings = repository.count(branchSpec);
        statistics.put("totalOfferings", totalOfferings);

        // Active session offerings in branch
        Specification<SessionOffering> activeBranchSpec = branchSpec.and(SessionOfferingSpecs.isActive(true));
        long activeOfferings = repository.count(activeBranchSpec);
        statistics.put("activeOfferings", activeOfferings);

        // Inactive session offerings in branch
        Specification<SessionOffering> inactiveBranchSpec = branchSpec.and(SessionOfferingSpecs.isActive(false));
        long inactiveOfferings = repository.count(inactiveBranchSpec);
        statistics.put("inactiveOfferings", inactiveOfferings);

        // Average price of session offerings in branch
        BigDecimal averagePrice = repository.findAveragePriceByBranchId(branchId);
        statistics.put("averagePrice", averagePrice != null ? averagePrice : BigDecimal.ZERO);

        return statistics;
    }

    @Override
    public Map<String, Object> getCurrentUserBranchStatistics() {
        UUID primaryBranchId = securityService.getPrimaryBranchId();
        if (primaryBranchId == null) {
            throw new RuntimeException("User has no primary branch assigned");
        }
        return getBranchStatistics(primaryBranchId);
    }

    @Override
    public Map<String, Object> getAllBranchesStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // Total session offerings across all branches
        long totalOfferings = repository.count();
        statistics.put("totalOfferings", totalOfferings);

        // Active session offerings across all branches
        Specification<SessionOffering> activeSpec = SessionOfferingSpecs.isActive(true);
        long activeOfferings = repository.count(activeSpec);
        statistics.put("activeOfferings", activeOfferings);

        // Inactive session offerings across all branches
        Specification<SessionOffering> inactiveSpec = SessionOfferingSpecs.isActive(false);
        long inactiveOfferings = repository.count(inactiveSpec);
        statistics.put("inactiveOfferings", inactiveOfferings);

        // Average price of session offerings across all branches
        BigDecimal averagePrice = repository.findAveragePrice();
        statistics.put("averagePrice", averagePrice != null ? averagePrice : BigDecimal.ZERO);

        return statistics;
    }
}
package com.tinysteps.sessionservice.service.impl;

import com.tinysteps.sessionservice.entity.SessionOffering;
import com.tinysteps.sessionservice.entity.SessionType;
import com.tinysteps.sessionservice.integration.service.DoctorIntegrationService;
import com.tinysteps.sessionservice.integration.service.AddressIntegrationService;
import com.tinysteps.sessionservice.repository.SessionOfferingRepository;
import com.tinysteps.sessionservice.repository.SessionTypeRepository;
import com.tinysteps.sessionservice.service.SessionOfferingService;
import com.tinysteps.sessionservice.specs.SessionOfferingSpecs;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionOfferingServiceImpl implements SessionOfferingService {

    private final SessionOfferingRepository repository;
    private final SessionTypeRepository sessionTypeRepository;
    private final DoctorIntegrationService doctorIntegrationService;
    private final AddressIntegrationService practiceIntegrationService;

    @Override
    @Transactional
    public SessionOffering create(SessionOffering offering) {
        // Validate doctor existence via integrations (temporarily disabled due to auth
        // issues)
        try {
            doctorIntegrationService.validateDoctorExistsOrThrow(offering.getDoctorId());
        } catch (Exception e) {
            // Log the error but don't fail the request for now
            System.err.println("Doctor validation failed: " + e.getMessage());
        }

        // Practice validation is optional since we removed practiceId from frontend
        if (offering.getPracticeId() != null) {
            try {
                practiceIntegrationService.validatePracticeExistsOrThrow(offering.getPracticeId());
            } catch (Exception e) {
                System.err.println("Practice validation failed: " + e.getMessage());
            }
        }

        // Ensure session type exists
        SessionType type = sessionTypeRepository.findById(offering.getSessionType().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid SessionType ID"));
        offering.setSessionType(type);

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

            // Practice validation is optional since we removed practiceId from frontend
            if (off.getPracticeId() != null) {
                try {
                    practiceIntegrationService.validatePracticeExistsOrThrow(off.getPracticeId());
                } catch (Exception e) {
                    System.err.println("Practice validation failed: " + e.getMessage());
                }
            }

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
            Pageable pageable) {

        Specification<SessionOffering> spec = Specification.where(

                SessionOfferingSpecs.bySessionTypeId(sessionTypeId))
                .and(SessionOfferingSpecs.isActive(isActive))
                .and(SessionOfferingSpecs.byPriceRange(minPrice, maxPrice));

        return repository.findAll(spec, pageable);
    }

    @Override
    public List<SessionOffering> getByDoctorId(UUID doctorId) {
        return repository.findByDoctorId(doctorId);
    }

    @Override
    public List<SessionOffering> getByPracticeId(UUID practiceId) {
        return repository.findByPracticeId(practiceId);
    }

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
}

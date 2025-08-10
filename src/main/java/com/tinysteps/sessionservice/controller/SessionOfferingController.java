package com.tinysteps.sessionservice.controller;

import com.tinysteps.sessionservice.entity.SessionOffering;
import com.tinysteps.sessionservice.service.SessionOfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors/{doctorId}/practices/{practiceId}/session-offerings")
@RequiredArgsConstructor
public class SessionOfferingController {

    private final SessionOfferingService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isDoctorOwner(authentication, #doctorId)")
    public SessionOffering create(@PathVariable UUID doctorId,
                                        @PathVariable UUID practiceId,
                                        @RequestBody SessionOffering offering) {
        // Enforce path consistency
        if (!offering.getDoctorId().equals(doctorId) || !offering.getPracticeId().equals(practiceId)) {
            throw new IllegalArgumentException("Doctor ID or Practice ID mismatch with path");
        }
        return service.create(offering);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isDoctorOwner(authentication, #doctorId)")
    public List<SessionOffering> bulkCreateOrUpdate(@PathVariable UUID doctorId,
                                                          @PathVariable UUID practiceId,
                                                          @RequestBody List<SessionOffering> offerings) {
        // Validate all belong to same doctor & practice
        offerings.forEach(off -> {
            if (!off.getDoctorId().equals(doctorId) || !off.getPracticeId().equals(practiceId)) {
                throw new IllegalArgumentException("All offerings must belong to same doctor and practice");
            }
        });
        return service.bulkCreateOrUpdate(offerings);
    }

    @GetMapping("/{offeringId}")
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isOfferingOwner(authentication, #offeringId)")
    public SessionOffering getById(@PathVariable UUID doctorId,
                                         @PathVariable UUID practiceId,
                                         @PathVariable UUID offeringId) {
        SessionOffering offering = service.getById(offeringId).orElseThrow(() -> new IllegalArgumentException("Offering not found"));
        if (!offering.getDoctorId().equals(doctorId) || !offering.getPracticeId().equals(practiceId)) {
            throw new IllegalArgumentException("Offering does not belong to specified doctor or practice");
        }
        return offering;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isDoctorOwner(authentication, #doctorId)")
    public Page<SessionOffering> search(@PathVariable UUID doctorId,
                                              @PathVariable UUID practiceId,
                                              @RequestParam(required = false) UUID sessionTypeId,
                                              @RequestParam(required = false) Boolean isActive,
                                              @RequestParam(required = false) BigDecimal minPrice,
                                              @RequestParam(required = false) BigDecimal maxPrice,
                                              Pageable pageable) {
        return service.search(doctorId, practiceId, sessionTypeId, isActive, minPrice, maxPrice, pageable);
    }

    @PutMapping("/{offeringId}")
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isOfferingOwner(authentication, #offeringId)")
    public SessionOffering update(@PathVariable UUID doctorId,
                                        @PathVariable UUID practiceId,
                                        @PathVariable UUID offeringId,
                                        @RequestBody SessionOffering offering) {
        if (!offering.getDoctorId().equals(doctorId) || !offering.getPracticeId().equals(practiceId)) {
            throw new IllegalArgumentException("Doctor ID or Practice ID mismatch with path");
        }
        return service.update(offeringId, offering);
    }

    @DeleteMapping("/{offeringId}")
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isOfferingOwner(authentication, #offeringId)")
    public void delete(@PathVariable UUID doctorId,
                       @PathVariable UUID practiceId,
                       @PathVariable UUID offeringId) {
        SessionOffering offering = service.getById(offeringId).orElseThrow(() -> new IllegalArgumentException("Offering not found"));
        if (!offering.getDoctorId().equals(doctorId) || !offering.getPracticeId().equals(practiceId)) {
            throw new IllegalArgumentException("Offering does not belong to specified doctor or practice");
        }
        service.delete(offeringId);
    }

    @PostMapping("/{offeringId}/activate")
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isOfferingOwner(authentication, #offeringId)")
    public SessionOffering activate(@PathVariable UUID offeringId) {
        return service.activate(offeringId);
    }

    @PostMapping("/{offeringId}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isOfferingOwner(authentication, #offeringId)")
    public SessionOffering deactivate(@PathVariable UUID offeringId) {
        return service.deactivate(offeringId);
    }

    @GetMapping("/by-practice")
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isDoctorOwner(authentication, #doctorId)")
    public Page<SessionOffering> getAllByPractice(@PathVariable UUID doctorId,
                                                        @RequestParam UUID practiceId,
                                                        Pageable pageable) {
        // This endpoint lists session offerings hospital-wise for a doctor
        return service.search(doctorId, practiceId, null, true, null, null, pageable);
    }
}

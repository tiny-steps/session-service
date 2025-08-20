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
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionOfferingController {

    private final SessionOfferingService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isDoctorOwner(authentication, #doctorId)")
    public SessionOffering create(
                                        @RequestBody SessionOffering offering) {

        return service.create(offering);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isDoctorOwner(authentication, #doctorId)")
    public List<SessionOffering> bulkCreateOrUpdate(
                                                          @RequestBody List<SessionOffering> offerings) {
        // Validate all belong to same doctor & practice

        return service.bulkCreateOrUpdate(offerings);
    }

    @GetMapping("/{offeringId}")
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isOfferingOwner(authentication, #offeringId)")
    public SessionOffering getById(
                                         @PathVariable UUID offeringId) {
        SessionOffering offering = service.getById(offeringId).orElseThrow(() -> new IllegalArgumentException("Offering not found"));

        return offering;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isDoctorOwner(authentication, #doctorId)")
    public Page<SessionOffering> search(
                                              @RequestParam(required = false) UUID sessionTypeId,
                                              @RequestParam(required = false) Boolean isActive,
                                              @RequestParam(required = false) BigDecimal minPrice,
                                              @RequestParam(required = false) BigDecimal maxPrice,
                                              Pageable pageable) {
        return service.search(sessionTypeId, isActive, minPrice, maxPrice, pageable);
    }

    @PutMapping("/{offeringId}")
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isOfferingOwner(authentication, #offeringId)")
    public SessionOffering update(
                                        @PathVariable UUID offeringId,
                                        @RequestBody SessionOffering offering) {

        return service.update(offeringId, offering);
    }

    @DeleteMapping("/{offeringId}")
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isOfferingOwner(authentication, #offeringId)")
    public void delete(
                       @PathVariable UUID offeringId) {
        SessionOffering offering = service.getById(offeringId).orElseThrow(() -> new IllegalArgumentException("Offering not found"));

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

}

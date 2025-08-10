package com.tinysteps.sessionservice.controller;

import com.tinysteps.sessionservice.entity.SessionType;
import com.tinysteps.sessionservice.service.SessionTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/session-types")
@RequiredArgsConstructor
public class SessionTypeController {

    private final SessionTypeService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SessionType create(@RequestBody SessionType sessionType) {
        return service.create(sessionType);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
    public SessionType getById(@PathVariable UUID id) {
        return service.getById(id).orElseThrow(() -> new IllegalArgumentException("Session Type not found"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
    public Page<SessionType> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isTelemedicineAvailable,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            Pageable pageable) {
        return service.search(name, isActive, isTelemedicineAvailable, minDuration, maxDuration, pageable);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SessionType update(@PathVariable UUID id, @RequestBody SessionType sessionType) {
        return service.update(id, sessionType);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public SessionType activate(@PathVariable UUID id) {
        return service.activate(id);
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public SessionType deactivate(@PathVariable UUID id) {
        return service.deactivate(id);
    }

    @GetMapping("/exists-by-name")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean existsByName(@RequestParam String name) {
        return service.existsByName(name);
    }
}

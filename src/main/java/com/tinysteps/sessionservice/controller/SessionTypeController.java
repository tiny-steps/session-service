package com.tinysteps.sessionservice.controller;

import com.tinysteps.sessionservice.entity.SessionType;
import com.tinysteps.sessionservice.service.SessionTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/session-types")
@RequiredArgsConstructor
@Tag(name = "Session Type Management", description = "APIs for managing session types")
@SecurityRequirement(name = "Bearer Authentication")
public class SessionTypeController {

    private final SessionTypeService service;

    @Operation(summary = "Create a new session type", description = "Creates a new session type with the provided information. Only accessible by ADMIN users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session type created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionType> create(@RequestBody SessionType sessionType) {
        return ResponseEntity.ok(service.create(sessionType));
    }

    @Operation(summary = "Get session type by ID", description = "Retrieves a session type by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session type found"),
            @ApiResponse(responseCode = "404", description = "Session type not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<SessionType> getById(@PathVariable UUID id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new IllegalArgumentException("Session Type not found"));
    }

    @Operation(summary = "Search session types", description = "Search for session types with multiple criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session types retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<Page<SessionType>> search(
            @Parameter(description = "Session type name") @RequestParam(required = false) String name,
            @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Telemedicine availability") @RequestParam(required = false) Boolean isTelemedicineAvailable,
            @Parameter(description = "Minimum duration in minutes") @RequestParam(required = false) Integer minDuration,
            @Parameter(description = "Maximum duration in minutes") @RequestParam(required = false) Integer maxDuration,
            @Parameter(description = "Pagination information") Pageable pageable) {
        return ResponseEntity
                .ok(service.search(name, isActive, isTelemedicineAvailable, minDuration, maxDuration, pageable));
    }

    @Operation(summary = "Update session type", description = "Updates a session type with new information. Only accessible by ADMIN users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session type updated successfully"),
            @ApiResponse(responseCode = "404", description = "Session type not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionType> update(@PathVariable UUID id, @RequestBody SessionType sessionType) {
        return ResponseEntity.ok(service.update(id, sessionType));
    }

    @Operation(summary = "Delete session type", description = "Deletes a session type. Only accessible by ADMIN users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session type deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Session type not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Activate session type", description = "Activates a session type. Only accessible by ADMIN users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session type activated successfully"),
            @ApiResponse(responseCode = "404", description = "Session type not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionType> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(service.activate(id));
    }

    @Operation(summary = "Deactivate session type", description = "Deactivates a session type. Only accessible by ADMIN users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session type deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Session type not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionType> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(service.deactivate(id));
    }

    @Operation(summary = "Check if session type name exists", description = "Checks if a session type with the given name already exists. Only accessible by ADMIN users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Existence check completed successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @GetMapping("/exists-by-name")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> existsByName(@RequestParam String name) {
        return ResponseEntity.ok(service.existsByName(name));
    }

    // ==================== NEW BRANCH-BASED ENDPOINTS ====================

    @Operation(summary = "Get all session types across all branches (Admin only)", description = "Retrieves a paginated list of all session types across all branches. Only accessible by ADMIN users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All session types retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @GetMapping("/all-branches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SessionType>> getAllSessionTypesAcrossAllBranches(
            @Parameter(description = "Session type name") @RequestParam(required = false) String name,
            @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Telemedicine availability") @RequestParam(required = false) Boolean isTelemedicineAvailable,
            @Parameter(description = "Minimum duration in minutes") @RequestParam(required = false) Integer minDuration,
            @Parameter(description = "Maximum duration in minutes") @RequestParam(required = false) Integer maxDuration,
            @Parameter(description = "Pagination information") Pageable pageable) {
        return ResponseEntity
                .ok(service.search(name, isActive, isTelemedicineAvailable, minDuration, maxDuration, pageable));
    }

    @Operation(summary = "Get session types for a specific branch", description = "Retrieves a paginated list of session types for a specific branch. Users can only access branches they have permission to view.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session types for branch retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - No permission to view this branch")
    })
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("@securityService.hasBranchAccess(#branchId) or hasRole('ADMIN')")
    public ResponseEntity<Page<SessionType>> getSessionTypesForBranch(
            @Parameter(description = "Branch ID", required = true) @PathVariable UUID branchId,
            @Parameter(description = "Session type name") @RequestParam(required = false) String name,
            @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Telemedicine availability") @RequestParam(required = false) Boolean isTelemedicineAvailable,
            @Parameter(description = "Minimum duration in minutes") @RequestParam(required = false) Integer minDuration,
            @Parameter(description = "Maximum duration in minutes") @RequestParam(required = false) Integer maxDuration,
            @Parameter(description = "Pagination information") Pageable pageable) {
        // Note: Session types are global entities, but we validate branch access for
        // consistency
        return ResponseEntity
                .ok(service.search(name, isActive, isTelemedicineAvailable, minDuration, maxDuration, pageable));
    }

    @Operation(summary = "Get session types for current user's branch", description = "Retrieves a paginated list of session types for the current user's primary branch.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session types for current user's branch retrieved successfully")
    })
    @GetMapping("/my-branch")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<Page<SessionType>> getSessionTypesForCurrentUserBranch(
            @Parameter(description = "Session type name") @RequestParam(required = false) String name,
            @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Telemedicine availability") @RequestParam(required = false) Boolean isTelemedicineAvailable,
            @Parameter(description = "Minimum duration in minutes") @RequestParam(required = false) Integer minDuration,
            @Parameter(description = "Maximum duration in minutes") @RequestParam(required = false) Integer maxDuration,
            @Parameter(description = "Pagination information") Pageable pageable) {
        // Note: Session types are global entities, but we validate branch access for
        // consistency
        return ResponseEntity
                .ok(service.search(name, isActive, isTelemedicineAvailable, minDuration, maxDuration, pageable));
    }

    @Operation(summary = "Get session type statistics for a specific branch", description = "Retrieves various statistics about session types in a specific branch.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Branch statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - No permission to view this branch")
    })
    @GetMapping("/statistics/branch/{branchId}")
    @PreAuthorize("@securityService.hasBranchAccess(#branchId) or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSessionTypeStatisticsForBranch(
            @Parameter(description = "Branch ID", required = true) @PathVariable UUID branchId) {
        // Note: Session types are global entities, but we validate branch access for
        // consistency
        Map<String, Object> statistics = service.getBranchStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Get session type statistics for current user's branch", description = "Retrieves various statistics about session types in the current user's primary branch.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current branch statistics retrieved successfully")
    })
    @GetMapping("/statistics/my-branch")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> getSessionTypeStatisticsForCurrentUserBranch() {
        Map<String, Object> statistics = service.getBranchStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Get session type statistics across all branches (Admin only)", description = "Retrieves various statistics about session types across all branches. Only accessible by ADMIN users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All branches statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @GetMapping("/statistics/all-branches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSessionTypeStatisticsAcrossAllBranches() {
        Map<String, Object> statistics = service.getGlobalStatistics();
        return ResponseEntity.ok(statistics);
    }
}
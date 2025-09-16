package com.tinysteps.sessionservice.controller;

import com.tinysteps.sessionservice.dto.SessionOfferingCreateDto;
import com.tinysteps.sessionservice.entity.SessionOffering;
import com.tinysteps.sessionservice.integration.constants.Status;
import com.tinysteps.sessionservice.service.SessionOfferingService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Session Offering Management", description = "APIs for managing session offerings")
@SecurityRequirement(name = "Bearer Authentication")
public class SessionOfferingController {

        private final SessionOfferingService service;

        @Operation(summary = "Create a new session offering", description = "Creates a new session offering with the provided information. Access is controlled by BranchValidationFilter.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Session offering created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @PostMapping
        @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
        public ResponseEntity<SessionOffering> create(@RequestBody SessionOfferingCreateDto createDto) {
                // Branch validation is now handled by BranchValidationFilter
                return ResponseEntity.ok(service.create(createDto));
        }

        @Operation(summary = "Bulk create or update session offerings", description = "Creates or updates multiple session offerings in bulk. Only accessible by ADMIN users.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Session offerings created/updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        @PostMapping("/bulk")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<List<SessionOffering>> bulkCreateOrUpdate(@RequestBody List<SessionOffering> offerings) {
                // Validate all belong to same doctor & practice
                return ResponseEntity.ok(service.bulkCreateOrUpdate(offerings));
        }

        @Operation(summary = "Get session offering by ID", description = "Retrieves a session offering by its unique identifier. Only accessible by ADMIN users.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Session offering found"),
                        @ApiResponse(responseCode = "404", description = "Session offering not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        @GetMapping("/{offeringId}")
        @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
        public ResponseEntity<SessionOffering> getById(@PathVariable UUID offeringId) {
                return service.getById(offeringId)
                                .map(ResponseEntity::ok)
                                .orElseThrow(() -> new IllegalArgumentException("Offering not found"));
        }

        @Operation(summary = "Search session offerings", description = "Search for session offerings with multiple criteria. Access is controlled by BranchValidationFilter.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Session offerings retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @GetMapping
        @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('RECEPTIONIST')")
        public ResponseEntity<Page<SessionOffering>> search(
                        @Parameter(description = "Session type ID") @RequestParam(required = false) UUID sessionTypeId,
                        @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
                        @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
                        @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
                        @Parameter(description = "Branch ID or 'all'") @RequestParam(required = false, name = "branchId") String branchIdStr,
                        @Parameter(description = "Pagination information") Pageable pageable) {

                UUID branchId = null;
                if (branchIdStr != null && !branchIdStr.isBlank() && !branchIdStr.equalsIgnoreCase("all")) {
                        branchId = UUID.fromString(branchIdStr); // will throw if invalid UUID (intentional)
                }
                // If branchIdStr == all -> branchId stays null meaning all branches (admin only validated in filter)
                return ResponseEntity
                                .ok(service.search(sessionTypeId, isActive, minPrice, maxPrice, branchId, pageable));
        }

        @Operation(summary = "Update session offering", description = "Updates a session offering with new information. Only accessible by ADMIN users.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Session offering updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Session offering not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        @PutMapping("/{offeringId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<SessionOffering> update(@PathVariable UUID offeringId,
                        @RequestBody SessionOffering offering) {
                return ResponseEntity.ok(service.update(offeringId, offering));
        }

        @Operation(summary = "Delete session offering", description = "Deletes a session offering. Only accessible by ADMIN users.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Session offering deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Session offering not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        @DeleteMapping("/{offeringId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> delete(@PathVariable UUID offeringId) {
                service.delete(offeringId);
                return ResponseEntity.ok().build();
        }

        @Operation(summary = "Activate session offering", description = "Activates a session offering. Only accessible by ADMIN users.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Session offering activated successfully"),
                        @ApiResponse(responseCode = "404", description = "Session offering not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        @PostMapping("/{offeringId}/activate")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<SessionOffering> activate(@PathVariable UUID offeringId) {
                return ResponseEntity.ok(service.activate(offeringId));
        }

        @Operation(summary = "Deactivate session offering", description = "Deactivates a session offering. Only accessible by ADMIN users.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Session offering deactivated successfully"),
                        @ApiResponse(responseCode = "404", description = "Session offering not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        @PostMapping("/{offeringId}/deactivate")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<SessionOffering> deactivate(@PathVariable UUID offeringId) {
                return ResponseEntity.ok(service.deactivate(offeringId));
        }

        // ==================== NEW BRANCH-BASED ENDPOINTS ====================

        @Operation(summary = "Get all session offerings across all branches (Admin only)", description = "Retrieves a paginated list of all session offerings across all branches. Only accessible by ADMIN users.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All session offerings retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        @GetMapping("/all-branches")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Page<SessionOffering>> getAllSessionOfferingsAcrossAllBranches(
                        @Parameter(description = "Session type ID") @RequestParam(required = false) UUID sessionTypeId,
                        @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
                        @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
                        @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
                        @Parameter(description = "Pagination information") Pageable pageable) {
                // Pass null for branchId to get all branches
                return ResponseEntity.ok(service.search(sessionTypeId, isActive, minPrice, maxPrice, null, pageable));
        }

        @Operation(summary = "Get session offerings for a specific branch", description = "Retrieves a paginated list of session offerings for a specific branch. Users can only access branches they have permission to view.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Session offerings for branch retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - No permission to view this branch")
        })
        @GetMapping("/branch/{branchId}")
        @PreAuthorize("@securityService.hasBranchAccess(#branchId) or hasRole('ADMIN')")
        public ResponseEntity<Page<SessionOffering>> getSessionOfferingsForBranch(
                        @Parameter(description = "Branch ID", required = true) @PathVariable UUID branchId,
                        @Parameter(description = "Session type ID") @RequestParam(required = false) UUID sessionTypeId,
                        @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
                        @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
                        @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
                        @Parameter(description = "Pagination information") Pageable pageable) {
                return ResponseEntity
                                .ok(service.search(sessionTypeId, isActive, minPrice, maxPrice, branchId, pageable));
        }

        @Operation(summary = "Get session offerings for current user's branch", description = "Retrieves a paginated list of session offerings for the current user's primary branch.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Session offerings for current user's branch retrieved successfully")
        })
        @GetMapping("/my-branch")
        @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
        public ResponseEntity<Page<SessionOffering>> getSessionOfferingsForCurrentUserBranch(
                        @Parameter(description = "Session type ID") @RequestParam(required = false) UUID sessionTypeId,
                        @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
                        @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
                        @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
                        @Parameter(description = "Pagination information") Pageable pageable) {
                // Branch validation will be handled by BranchValidationFilter using primary
                // branch
                return ResponseEntity
                                .ok(service.searchForCurrentUserBranch(sessionTypeId, isActive, minPrice, maxPrice,
                                                pageable));
        }

        @Operation(summary = "Search session offerings across all branches (Admin only)", description = "Advanced search for session offerings across all branches. Only accessible by ADMIN users.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Search across all branches completed successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        @GetMapping("/search/all-branches")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Page<SessionOffering>> searchSessionOfferingsAcrossAllBranches(
                        @Parameter(description = "Session type ID") @RequestParam(required = false) UUID sessionTypeId,
                        @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
                        @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
                        @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
                        @Parameter(description = "Pagination information") Pageable pageable) {
                // Pass null for branchId to search across all branches
                return ResponseEntity.ok(service.search(sessionTypeId, isActive, minPrice, maxPrice, null, pageable));
        }

        @Operation(summary = "Search session offerings in a specific branch", description = "Advanced search for session offerings within a specific branch. Users can only search in branches they have permission to view.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Search in branch completed successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - No permission to view this branch")
        })
        @GetMapping("/search/branch/{branchId}")
        @PreAuthorize("@securityService.hasBranchAccess(#branchId) or hasRole('ADMIN')")
        public ResponseEntity<Page<SessionOffering>> searchSessionOfferingsInBranch(
                        @Parameter(description = "Branch ID", required = true) @PathVariable UUID branchId,
                        @Parameter(description = "Session type ID") @RequestParam(required = false) UUID sessionTypeId,
                        @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
                        @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
                        @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
                        @Parameter(description = "Pagination information") Pageable pageable) {
                return ResponseEntity
                                .ok(service.search(sessionTypeId, isActive, minPrice, maxPrice, branchId, pageable));
        }

        @Operation(summary = "Get session offering statistics for a specific branch", description = "Retrieves various statistics about session offerings in a specific branch.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Branch statistics retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - No permission to view this branch")
        })
        @GetMapping("/statistics/branch/{branchId}")
        @PreAuthorize("@securityService.hasBranchAccess(#branchId) or hasRole('ADMIN')")
        public ResponseEntity<Map<String, Object>> getSessionOfferingStatisticsForBranch(
                        @Parameter(description = "Branch ID", required = true) @PathVariable UUID branchId) {
                Map<String, Object> statistics = service.getBranchStatistics(branchId);
                return ResponseEntity.ok(statistics);
        }

        @Operation(summary = "Get session offering statistics for current user's branch", description = "Retrieves various statistics about session offerings in the current user's primary branch.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Current branch statistics retrieved successfully")
        })
        @GetMapping("/statistics/my-branch")
        @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
        public ResponseEntity<Map<String, Object>> getSessionOfferingStatisticsForCurrentUserBranch() {
                Map<String, Object> statistics = service.getCurrentUserBranchStatistics();
                return ResponseEntity.ok(statistics);
        }

        @Operation(summary = "Get session offering statistics across all branches (Admin only)", description = "Retrieves various statistics about session offerings across all branches. Only accessible by ADMIN users.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All branches statistics retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        @GetMapping("/statistics/all-branches")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Map<String, Object>> getSessionOfferingStatisticsAcrossAllBranches() {
                Map<String, Object> statistics = service.getAllBranchesStatistics();
                return ResponseEntity.ok(statistics);
        }

        // Soft Delete Endpoints
        @Operation(summary = "Soft delete session offering", description = "Soft deletes a session offering by setting its status to DELETED. Only accessible by ADMIN users.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Session offering soft deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Session offering not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        @PostMapping("/{offeringId}/soft-delete")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<SessionOffering> softDelete(@PathVariable UUID offeringId) {
                SessionOffering softDeletedOffering = service.softDelete(offeringId);
                return ResponseEntity.ok(softDeletedOffering);
        }

        @Operation(summary = "Reactivate session offering", description = "Reactivates a soft deleted session offering by setting its status to ACTIVE. Only accessible by ADMIN users.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Session offering reactivated successfully"),
                        @ApiResponse(responseCode = "404", description = "Session offering not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        })
        @PostMapping("/{offeringId}/reactivate")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<SessionOffering> reactivate(@PathVariable UUID offeringId) {
                SessionOffering reactivatedOffering = service.reactivate(offeringId);
                return ResponseEntity.ok(reactivatedOffering);
        }

        @Operation(summary = "Get active session offerings", description = "Retrieves all active session offerings")
        @GetMapping("/active")
        @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
        public ResponseEntity<Page<SessionOffering>> getActiveSessionOfferings(Pageable pageable) {
                Page<SessionOffering> activeOfferings = service.findOfferingsByStatus(Status.ACTIVE, pageable);
                return ResponseEntity.ok(activeOfferings);
        }

        @Operation(summary = "Get deleted session offerings", description = "Retrieves all deleted session offerings")
        @GetMapping("/deleted")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Page<SessionOffering>> getDeletedSessionOfferings(Pageable pageable) {
                Page<SessionOffering> deletedOfferings = service.findOfferingsByStatus(Status.DELETED, pageable);
                return ResponseEntity.ok(deletedOfferings);
        }
}

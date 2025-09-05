package com.tinysteps.sessionservice.controller;

import com.tinysteps.sessionservice.dto.SessionTransferRequestDto;
import com.tinysteps.sessionservice.dto.SessionTransferResponseDto;
import com.tinysteps.sessionservice.service.SessionTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for managing session transfers between branches.
 * Provides endpoints for transferring session offerings, checking transfer status,
 * and handling emergency transfers.
 */
@RestController
@RequestMapping("/api/v1/sessions/transfer")
@RequiredArgsConstructor
@Validated
@Slf4j
public class SessionTransferController {

    private final SessionTransferService sessionTransferService;

    /**
     * Transfer sessions between branches based on the transfer request.
     * Supports bulk, selective, date range, and emergency transfers.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER')")
    public ResponseEntity<SessionTransferResponseDto> transferSessions(
            @Valid @RequestBody SessionTransferRequestDto request) {
        
        log.info("Received session transfer request from branch {} to branch {} with type {}", 
                request.getSourceBranchId(), request.getTargetBranchId(), request.getTransferType());
        
        try {
            SessionTransferResponseDto response = sessionTransferService.transferSessions(request);
            
            HttpStatus status = switch (response.status()) {
                case "COMPLETED" -> HttpStatus.OK;
                case "COMPLETED_WITH_ERRORS" -> HttpStatus.PARTIAL_CONTENT;
                case "FAILED" -> HttpStatus.BAD_REQUEST;
                default -> HttpStatus.ACCEPTED; // IN_PROGRESS
            };
            
            return ResponseEntity.status(status).body(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid session transfer request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                SessionTransferResponseDto.builder()
                    .transferId(UUID.randomUUID())
                    .status("FAILED")
                    .message("Invalid request: " + e.getMessage())
                    .sourceBranchId(request.sourceBranchId())
                    .targetBranchId(request.targetBranchId())
                    .transferredAt(ZonedDateTime.now().toLocalDateTime())
                    .completedAt(ZonedDateTime.now().toLocalDateTime())
                    .build()
            );
        } catch (Exception e) {
            log.error("Session transfer failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SessionTransferResponseDto.builder()
                    .transferId(UUID.randomUUID())
                    .status("FAILED")
                    .message("Internal server error: " + e.getMessage())
                    .sourceBranchId(request.getSourceBranchId())
                    .targetBranchId(request.getTargetBranchId())
                    .transferredAt(ZonedDateTime.now().toLocalDateTime())
                    .completedAt(ZonedDateTime.now().toLocalDateTime())
                    .build()
            );
        }
    }

    /**
     * Transfer sessions by date range.
     */
    @PostMapping("/by-date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER')")
    public ResponseEntity<SessionTransferResponseDto> transferSessionsByDateRange(
            @RequestParam @NotNull UUID sourceBranchId,
            @RequestParam @NotNull UUID targetBranchId,
            @RequestParam @NotNull ZonedDateTime startDate,
            @RequestParam @NotNull ZonedDateTime endDate,
            @RequestParam(required = false) String reason) {
        
        log.info("Transferring sessions by date range from {} to {} between {} and {}", 
                sourceBranchId, targetBranchId, startDate, endDate);
        
        try {
            SessionTransferResponseDto response = sessionTransferService.transferSessionsByDateRange(
                    sourceBranchId, targetBranchId, startDate.toLocalDate(), endDate.toLocalDate(), reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Date range session transfer failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SessionTransferResponseDto.builder()
                    .transferId(UUID.randomUUID())
                    .status("FAILED")
                    .message("Transfer failed: " + e.getMessage())
                    .sourceBranchId(sourceBranchId)
                    .targetBranchId(targetBranchId)
                    .transferredAt(ZonedDateTime.now().toLocalDateTime())
                    .completedAt(ZonedDateTime.now().toLocalDateTime())
                    .build()
            );
        }
    }

    /**
     * Transfer specific sessions by their IDs.
     */
    @PostMapping("/by-ids")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER')")
    public ResponseEntity<SessionTransferResponseDto> transferSessionsByIds(
            @RequestParam @NotNull UUID sourceBranchId,
            @RequestParam @NotNull UUID targetBranchId,
            @RequestBody @NotNull List<UUID> sessionIds,
            @RequestParam(required = false) String reason) {
        
        log.info("Transferring {} specific sessions from {} to {}", 
                sessionIds.size(), sourceBranchId, targetBranchId);
        
        try {
            SessionTransferResponseDto response = sessionTransferService.transferSessionsByIds(
                    sourceBranchId, targetBranchId, sessionIds, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Selective session transfer failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SessionTransferResponseDto.builder()
                    .transferId(UUID.randomUUID())
                    .status("FAILED")
                    .message("Transfer failed: " + e.getMessage())
                    .sourceBranchId(sourceBranchId)
                    .targetBranchId(targetBranchId)
                    .transferredAt(ZonedDateTime.now().toLocalDateTime())
                    .completedAt(ZonedDateTime.now().toLocalDateTime())
                    .build()
            );
        }
    }

    /**
     * Perform an emergency transfer of all active sessions.
     */
    @PostMapping("/emergency")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionTransferResponseDto> emergencyTransfer(
            @RequestParam @NotNull UUID sourceBranchId,
            @RequestParam @NotNull UUID targetBranchId,
            @RequestParam @NotNull String reason) {
        
        log.warn("Emergency session transfer initiated from {} to {} - Reason: {}", 
                sourceBranchId, targetBranchId, reason);
        
        try {
            SessionTransferResponseDto response = sessionTransferService.emergencyTransfer(
                    sourceBranchId, targetBranchId, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Emergency session transfer failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SessionTransferResponseDto.builder()
                    .transferId(UUID.randomUUID())
                    .status("FAILED")
                    .message("Emergency transfer failed: " + e.getMessage())
                    .sourceBranchId(sourceBranchId)
                    .targetBranchId(targetBranchId)
                    .transferredAt(ZonedDateTime.now().toLocalDateTime())
                    .completedAt(ZonedDateTime.now().toLocalDateTime())
                    .build()
            );
        }
    }

    /**
     * Check the status of a transfer operation.
     */
    @GetMapping("/status/{transferId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER')")
    public ResponseEntity<SessionTransferResponseDto> getTransferStatus(
            @PathVariable @NotNull String transferId) {
        
        log.debug("Checking status for transfer ID: {}", transferId);
        
        SessionTransferResponseDto status = sessionTransferService.getTransferStatus(transferId);
        
        if (status != null) {
            return ResponseEntity.ok(status);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if sessions can be transferred between branches.
     */
    @GetMapping("/eligibility")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BRANCH_MANAGER')")
    public ResponseEntity<Boolean> canTransferSessions(
            @RequestParam @NotNull UUID sourceBranchId,
            @RequestParam @NotNull UUID targetBranchId) {
        
        log.debug("Checking transfer eligibility from {} to {}", sourceBranchId, targetBranchId);
        
        boolean canTransfer = sessionTransferService.canTransferSessions(sourceBranchId, targetBranchId);
        return ResponseEntity.ok(canTransfer);
    }

    /**
     * Exception handler for validation errors.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleValidationException(IllegalArgumentException e) {
        log.error("Validation error: {}", e.getMessage());
        return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
    }

    /**
     * Exception handler for general errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        log.error("Unexpected error in session transfer: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error: " + e.getMessage());
    }
}
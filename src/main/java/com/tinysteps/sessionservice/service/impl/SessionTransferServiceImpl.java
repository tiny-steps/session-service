package com.tinysteps.sessionservice.service.impl;

import com.tinysteps.sessionservice.dto.SessionTransferRequestDto;
import com.tinysteps.sessionservice.dto.SessionTransferResponseDto;
import com.tinysteps.sessionservice.entity.SessionOffering;
import com.tinysteps.sessionservice.repository.SessionOfferingRepository;
import com.tinysteps.sessionservice.service.SessionTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionTransferServiceImpl implements SessionTransferService {

    private final SessionOfferingRepository sessionOfferingRepository;
    
    // In-memory storage for transfer status (in production, use Redis or database)
    private final Map<UUID, SessionTransferResponseDto> transferStatusMap = new HashMap<>();

    @Override
    @Transactional
    public SessionTransferResponseDto transferSessions(SessionTransferRequestDto request) {
        log.info("Starting session transfer from branch {} to branch {}", 
                request.sourceBranchId(), request.targetBranchId());
        
        UUID transferId = UUID.randomUUID();
        
        // Initialize collections for building the response
        List<SessionTransferResponseDto.SessionTransferResult> detailedResults = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try {
            // Validate branches
            validateBranches(request.sourceBranchId(), request.targetBranchId());
            
            List<SessionOffering> sessionsToTransfer = getSessionsToTransfer(request);
            
            if (sessionsToTransfer.isEmpty()) {
                SessionTransferResponseDto response = SessionTransferResponseDto.builder()
                        .transferId(transferId)
                        .status("COMPLETED")
                        .message("No sessions found to transfer")
                        .sourceBranchId(request.sourceBranchId())
                         .targetBranchId(request.targetBranchId())
                         .transferredAt(LocalDateTime.now())
                         .completedAt(LocalDateTime.now())
                         .summary(SessionTransferResponseDto.TransferSummary.builder()
                                 .totalSessions(0)
                                 .successfulTransfers(0)
                                 .failedTransfers(0)
                                 .skippedSessions(0)
                                 .transferType(request.transferType().toString())
                                 .build())
                        .results(detailedResults)
                         .warnings(warnings)
                         .errors(errors)
                         .rollbackInfo(null)
                         .build();
                transferStatusMap.put(transferId, response);
                return response;
            }
            
            // Perform the transfer
            int successCount = 0;
            int failureCount = 0;
            
            for (SessionOffering session : sessionsToTransfer) {
                try {
                    SessionOffering transferredSession = transferSingleSession(session, request);
                    detailedResults.add(SessionTransferResponseDto.SessionTransferResult.builder()
                             .sessionId(session.getId())
                             .sessionTitle(session.getSessionType().getName())
                             .status("SUCCESS")
                             .reason("Session transferred successfully")
                             .originalStartTime(null)
                             .newStartTime(null)
                             .originalBranchId(request.sourceBranchId())
                             .newBranchId(request.targetBranchId())
                             .build());
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to transfer session {}: {}", session.getId(), e.getMessage());
                    detailedResults.add(SessionTransferResponseDto.SessionTransferResult.builder()
                             .sessionId(session.getId())
                             .sessionTitle(session.getSessionType().getName())
                             .status("FAILED")
                             .reason("Transfer failed: " + e.getMessage())
                             .originalStartTime(null)
                             .newStartTime(null)
                             .originalBranchId(request.sourceBranchId())
                             .newBranchId(request.targetBranchId())
                             .build());
                    failureCount++;
                }
            }
            
            // Build final response
            SessionTransferResponseDto response = SessionTransferResponseDto.builder()
                    .transferId(transferId)
                    .status(failureCount == 0 ? "COMPLETED" : "COMPLETED_WITH_ERRORS")
                    .message(String.format("Transfer completed. %d successful, %d failed", 
                            successCount, failureCount))
                    .sourceBranchId(request.sourceBranchId())
                     .targetBranchId(request.targetBranchId())
                     .transferredAt(LocalDateTime.now())
                     .completedAt(LocalDateTime.now())
                     .summary(SessionTransferResponseDto.TransferSummary.builder()
                             .totalSessions(sessionsToTransfer.size())
                             .successfulTransfers(successCount)
                             .failedTransfers(failureCount)
                             .skippedSessions(0)
                             .transferType(request.transferType().toString())
                             .build())
                     .results(detailedResults)
                     .warnings(warnings)
                     .errors(errors)
                     .rollbackInfo(null)
                    .build();
            
            transferStatusMap.put(transferId, response);
            return response;
            
        } catch (Exception e) {
            log.error("Session transfer failed: {}", e.getMessage(), e);
            errors.add(e.getMessage());
            
            SessionTransferResponseDto response = SessionTransferResponseDto.builder()
                    .transferId(transferId)
                    .status("FAILED")
                    .message("Transfer failed: " + e.getMessage())
                    .sourceBranchId(request.sourceBranchId())
                     .targetBranchId(request.targetBranchId())
                     .transferredAt(LocalDateTime.now())
                     .completedAt(LocalDateTime.now())
                     .summary(null)
                     .results(detailedResults)
                     .warnings(warnings)
                     .errors(errors)
                     .rollbackInfo(null)
                    .build();
            
            transferStatusMap.put(transferId, response);
            return response;
        }
    }



    @Override
    public boolean canTransferSessions(UUID sourceBranchId, UUID targetBranchId) {
        try {
            validateBranches(sourceBranchId, targetBranchId);
            return true;
        } catch (Exception e) {
            log.warn("Cannot transfer sessions from {} to {}: {}", 
                    sourceBranchId, targetBranchId, e.getMessage());
            return false;
        }
    }

    @Override
    public SessionTransferResponseDto getTransferStatus(String transferId) {
        try {
            UUID uuid = UUID.fromString(transferId);
            return transferStatusMap.get(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public SessionTransferResponseDto transferSessionsByDateRange(
            UUID sourceBranchId, 
            UUID targetBranchId, 
            LocalDate startDate, 
            LocalDate endDate,
            String reason) {
        
        SessionTransferRequestDto request = SessionTransferRequestDto.builder()
                .sourceBranchId(sourceBranchId)
                .targetBranchId(targetBranchId)
                .transferType(SessionTransferRequestDto.TransferType.DATE_RANGE)
                .startDate(startDate.atStartOfDay(ZoneId.systemDefault()))
                .endDate(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()))
                .reason(reason)
                .emergencyFlag(false)
                .build();
        
        return transferSessions(request);
    }

    @Override
    public SessionTransferResponseDto transferSessionsByIds(
            UUID sourceBranchId,
            UUID targetBranchId,
            List<UUID> sessionIds,
            String reason) {
        
        SessionTransferRequestDto request = SessionTransferRequestDto.builder()
                .sourceBranchId(sourceBranchId)
                .targetBranchId(targetBranchId)
                .transferType(SessionTransferRequestDto.TransferType.SELECTIVE)
                .sessionIds(sessionIds)
                .reason(reason)
                .emergencyFlag(false)
                .build();
        
        return transferSessions(request);
    }

    @Override
    public SessionTransferResponseDto emergencyTransfer(
            UUID sourceBranchId,
            UUID targetBranchId,
            String reason) {
        
        SessionTransferRequestDto request = SessionTransferRequestDto.builder()
                .sourceBranchId(sourceBranchId)
                .targetBranchId(targetBranchId)
                .transferType(SessionTransferRequestDto.TransferType.EMERGENCY)
                .reason(reason)
                .emergencyFlag(true)
                .build();
        
        return transferSessions(request);
    }

    private void validateBranches(UUID sourceBranchId, UUID targetBranchId) {
        if (sourceBranchId == null) {
            throw new IllegalArgumentException("Source branch ID cannot be null");
        }
        if (targetBranchId == null) {
            throw new IllegalArgumentException("Target branch ID cannot be null");
        }
        if (sourceBranchId.equals(targetBranchId)) {
            throw new IllegalArgumentException("Source and target branches cannot be the same");
        }
        
        // TODO: Add actual branch validation via address-service integration
        // For now, we'll assume branches are valid
    }

    private List<SessionOffering> getSessionsToTransfer(SessionTransferRequestDto request) {
        switch (request.transferType()) {
            case BULK:
                return sessionOfferingRepository.findByBranchId(request.sourceBranchId());
            
            case SELECTIVE:
                if (request.sessionIds() == null || request.sessionIds().isEmpty()) {
                    throw new IllegalArgumentException("Session IDs are required for selective transfer");
                }
                return sessionOfferingRepository.findByIdIn(request.sessionIds())
                        .stream()
                        .filter(session -> session.getBranchId().equals(request.sourceBranchId()))
                        .collect(Collectors.toList());
            
            case DATE_RANGE:
                if (request.startDate() == null || request.endDate() == null) {
                    throw new IllegalArgumentException("Start and end dates are required for date range transfer");
                }
                return sessionOfferingRepository.findByBranchIdAndCreatedAtBetween(
                        request.sourceBranchId(), request.startDate(), request.endDate());
            
            case EMERGENCY:
                // For emergency transfers, transfer all active sessions
                return sessionOfferingRepository.findByBranchId(request.sourceBranchId())
                        .stream()
                        .filter(SessionOffering::isActive)
                        .collect(Collectors.toList());
            
            default:
                throw new IllegalArgumentException("Unsupported transfer type: " + request.transferType());
        }
    }

    private SessionOffering transferSingleSession(SessionOffering session, SessionTransferRequestDto request) {
        // Create a copy of the session for the target branch
        SessionOffering transferredSession = SessionOffering.builder()
                .doctorId(session.getDoctorId())
                .branchId(request.targetBranchId())
                .sessionType(session.getSessionType())
                .price(session.getPrice())
                .isActive(session.isActive())
                .build();
        
        // Save the transferred session
        SessionOffering savedSession = sessionOfferingRepository.save(transferredSession);
        
        // Handle original session based on transfer options
        if (!request.preserveOriginalSchedule()) {
            // Remove or deactivate the original session
            if (request.emergencyFlag()) {
                // For emergency transfers, deactivate the original
                session.setActive(false);
                sessionOfferingRepository.save(session);
            } else {
                // For regular transfers, delete the original
                sessionOfferingRepository.delete(session);
            }
        }
        
        return savedSession;
    }
}
package com.tinysteps.sessionservice.service;

import com.tinysteps.sessionservice.dto.SessionTransferRequestDto;
import com.tinysteps.sessionservice.dto.SessionTransferResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SessionTransferService {

    /**
     * Transfer session offerings from one branch to another
     */
    SessionTransferResponseDto transferSessions(SessionTransferRequestDto requestDto);

    /**
     * Transfer sessions by date range
     */
    SessionTransferResponseDto transferSessionsByDateRange(
            UUID sourceBranchId, 
            UUID targetBranchId, 
            LocalDate startDate, 
            LocalDate endDate,
            String reason);

    /**
     * Transfer specific session offerings by IDs
     */
    SessionTransferResponseDto transferSessionsByIds(
            UUID sourceBranchId,
            UUID targetBranchId,
            List<UUID> sessionIds,
            String reason);

    /**
     * Emergency transfer - immediate transfer with minimal validation
     */
    SessionTransferResponseDto emergencyTransfer(
            UUID sourceBranchId,
            UUID targetBranchId,
            String reason);

    /**
     * Check if sessions can be transferred between branches
     */
    boolean canTransferSessions(UUID sourceBranchId, UUID targetBranchId);

    /**
     * Get transfer status by transfer ID
     */
    SessionTransferResponseDto getTransferStatus(String transferId);
}
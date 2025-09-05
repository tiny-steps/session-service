package com.tinysteps.sessionservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record SessionTransferResponseDto(
        UUID transferId,
        
        String status, // SUCCESS, FAILED, PARTIAL, IN_PROGRESS
        
        String message,
        
        UUID sourceBranchId,
        
        UUID targetBranchId,
        
        LocalDateTime transferredAt,
        
        LocalDateTime completedAt,
        
        // Transfer summary
        TransferSummary summary,
        
        // Detailed results
        List<SessionTransferResult> results,
        
        // Warnings and errors
        List<String> warnings,
        
        List<String> errors,
        
        // Rollback information
        RollbackInfo rollbackInfo
) {
    
    public static SessionTransferResponseDtoBuilder builder() {
        return new SessionTransferResponseDtoBuilder();
    }
    
    public static class SessionTransferResponseDtoBuilder {
        private UUID transferId;
        private String status;
        private String message;
        private UUID sourceBranchId;
        private UUID targetBranchId;
        private LocalDateTime transferredAt;
        private LocalDateTime completedAt;
        private TransferSummary summary;
        private List<SessionTransferResult> results;
        private List<String> warnings;
        private List<String> errors;
        private RollbackInfo rollbackInfo;
        
        public SessionTransferResponseDtoBuilder transferId(UUID transferId) {
            this.transferId = transferId;
            return this;
        }
        
        public SessionTransferResponseDtoBuilder status(String status) {
            this.status = status;
            return this;
        }
        
        public SessionTransferResponseDtoBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public SessionTransferResponseDtoBuilder sourceBranchId(UUID sourceBranchId) {
            this.sourceBranchId = sourceBranchId;
            return this;
        }
        
        public SessionTransferResponseDtoBuilder targetBranchId(UUID targetBranchId) {
            this.targetBranchId = targetBranchId;
            return this;
        }
        
        public SessionTransferResponseDtoBuilder transferredAt(LocalDateTime transferredAt) {
            this.transferredAt = transferredAt;
            return this;
        }
        
        public SessionTransferResponseDtoBuilder completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }
        
        public SessionTransferResponseDtoBuilder summary(TransferSummary summary) {
            this.summary = summary;
            return this;
        }
        
        public SessionTransferResponseDtoBuilder results(List<SessionTransferResult> results) {
            this.results = results;
            return this;
        }
        
        public SessionTransferResponseDtoBuilder warnings(List<String> warnings) {
            this.warnings = warnings;
            return this;
        }
        
        public SessionTransferResponseDtoBuilder errors(List<String> errors) {
            this.errors = errors;
            return this;
        }
        
        public SessionTransferResponseDtoBuilder rollbackInfo(RollbackInfo rollbackInfo) {
            this.rollbackInfo = rollbackInfo;
            return this;
        }
        
        public SessionTransferResponseDto build() {
            return new SessionTransferResponseDto(transferId, status, message, sourceBranchId, 
                targetBranchId, transferredAt, completedAt, summary, results, warnings, errors, rollbackInfo);
        }
    }
    @Builder
    public record TransferSummary(
            int totalSessions,
            int successfulTransfers,
            int failedTransfers,
            int skippedSessions,
            String transferType
    ) {
        public static TransferSummaryBuilder builder() {
            return new TransferSummaryBuilder();
        }
        
        public static class TransferSummaryBuilder {
            private int totalSessions;
            private int successfulTransfers;
            private int failedTransfers;
            private int skippedSessions;
            private String transferType;
            
            public TransferSummaryBuilder totalSessions(int totalSessions) {
                this.totalSessions = totalSessions;
                return this;
            }
            
            public TransferSummaryBuilder successfulTransfers(int successfulTransfers) {
                this.successfulTransfers = successfulTransfers;
                return this;
            }
            
            public TransferSummaryBuilder failedTransfers(int failedTransfers) {
                this.failedTransfers = failedTransfers;
                return this;
            }
            
            public TransferSummaryBuilder skippedSessions(int skippedSessions) {
                this.skippedSessions = skippedSessions;
                return this;
            }
            
            public TransferSummaryBuilder transferType(String transferType) {
                this.transferType = transferType;
                return this;
            }
            
            public TransferSummary build() {
                return new TransferSummary(totalSessions, successfulTransfers, failedTransfers, skippedSessions, transferType);
            }
        }
    }
    
    @Builder
    public record SessionTransferResult(
            UUID sessionId,
            String sessionTitle,
            String status, // SUCCESS, FAILED, SKIPPED
            String reason,
            LocalDateTime originalStartTime,
            LocalDateTime newStartTime,
            UUID originalBranchId,
            UUID newBranchId
    ) {}
    
    @Builder
    public record RollbackInfo(
            boolean rollbackAvailable,
            String rollbackReason,
            LocalDateTime rollbackDeadline,
            List<UUID> rollbackSessionIds
    ) {}
}
package com.tinysteps.sessionservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record SessionTransferRequestDto(
        @NotNull(message = "Source branch ID is required")
        UUID sourceBranchId,
        
        @NotNull(message = "Target branch ID is required")
        UUID targetBranchId,
        
        @NotNull(message = "Transfer type is required")
        TransferType transferType,
        
        String reason,
        
        String notes,
        
        // For selective transfers
        List<UUID> sessionIds,
        
        // For date range transfers
        ZonedDateTime startDate,
        
        ZonedDateTime endDate,
        
        // Transfer options
        Boolean preserveOriginalSchedule,
        
        Boolean notifyParticipants,
        
        Boolean maintainSessionTypes,
        
        // Emergency transfer flag
        Boolean emergencyFlag
) {
    
    public enum TransferType {
        BULK, SELECTIVE, EMERGENCY, DATE_RANGE
    }
    public SessionTransferRequestDto {
        // Default values
        if (preserveOriginalSchedule == null) {
            preserveOriginalSchedule = true;
        }
        if (notifyParticipants == null) {
            notifyParticipants = true;
        }
        if (maintainSessionTypes == null) {
            maintainSessionTypes = true;
        }
        if (emergencyFlag == null) {
            emergencyFlag = false;
        }
    }

    // Getter methods for record compatibility
    public UUID getSourceBranchId() {
        return sourceBranchId;
    }

    public UUID getTargetBranchId() {
        return targetBranchId;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public String getReason() {
        return reason;
    }

    public String getNotes() {
        return notes;
    }

    public List<UUID> getSessionIds() {
        return sessionIds;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public Boolean getPreserveOriginalSchedule() {
        return preserveOriginalSchedule;
    }

    public Boolean getNotifyParticipants() {
        return notifyParticipants;
    }

    public Boolean getMaintainSessionTypes() {
        return maintainSessionTypes;
    }

    public Boolean getEmergencyFlag() {
        return emergencyFlag;
    }

    // Manual builder method for compilation
    public static SessionTransferRequestDtoBuilder builder() {
        return new SessionTransferRequestDtoBuilder();
    }

    public static class SessionTransferRequestDtoBuilder {
        private UUID sourceBranchId;
        private UUID targetBranchId;
        private TransferType transferType;
        private String reason;
        private String notes;
        private List<UUID> sessionIds;
        private ZonedDateTime startDate;
        private ZonedDateTime endDate;
        private Boolean preserveOriginalSchedule;
        private Boolean notifyParticipants;
        private Boolean maintainSessionTypes;
        private Boolean emergencyFlag;

        public SessionTransferRequestDtoBuilder sourceBranchId(UUID sourceBranchId) {
            this.sourceBranchId = sourceBranchId;
            return this;
        }

        public SessionTransferRequestDtoBuilder targetBranchId(UUID targetBranchId) {
            this.targetBranchId = targetBranchId;
            return this;
        }

        public SessionTransferRequestDtoBuilder transferType(TransferType transferType) {
            this.transferType = transferType;
            return this;
        }

        public SessionTransferRequestDtoBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public SessionTransferRequestDtoBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public SessionTransferRequestDtoBuilder sessionIds(List<UUID> sessionIds) {
            this.sessionIds = sessionIds;
            return this;
        }

        public SessionTransferRequestDtoBuilder startDate(ZonedDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public SessionTransferRequestDtoBuilder endDate(ZonedDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public SessionTransferRequestDtoBuilder preserveOriginalSchedule(Boolean preserveOriginalSchedule) {
            this.preserveOriginalSchedule = preserveOriginalSchedule;
            return this;
        }

        public SessionTransferRequestDtoBuilder notifyParticipants(Boolean notifyParticipants) {
            this.notifyParticipants = notifyParticipants;
            return this;
        }

        public SessionTransferRequestDtoBuilder maintainSessionTypes(Boolean maintainSessionTypes) {
            this.maintainSessionTypes = maintainSessionTypes;
            return this;
        }

        public SessionTransferRequestDtoBuilder emergencyFlag(Boolean emergencyFlag) {
            this.emergencyFlag = emergencyFlag;
            return this;
        }

        public SessionTransferRequestDto build() {
            return new SessionTransferRequestDto(
                sourceBranchId,
                targetBranchId,
                transferType,
                reason,
                notes,
                sessionIds,
                startDate,
                endDate,
                preserveOriginalSchedule,
                notifyParticipants,
                maintainSessionTypes,
                emergencyFlag
            );
        }
    }
}
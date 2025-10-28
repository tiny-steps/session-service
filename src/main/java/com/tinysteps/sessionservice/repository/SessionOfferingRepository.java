package com.tinysteps.sessionservice.repository;

import com.tinysteps.sessionservice.entity.SessionOffering;
import com.tinysteps.sessionservice.entity.SessionType;
import com.tinysteps.sessionservice.integration.constants.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionOfferingRepository
                extends JpaRepository<SessionOffering, UUID>,
                JpaSpecificationExecutor<SessionOffering> {

        List<SessionOffering> findByDoctorId(UUID doctorId);

        // findByPracticeId method removed - no longer needed after Practice entity
        // removal
        // findByDoctorIdAndPracticeId method removed - no longer needed after Practice
        // entity removal
        List<SessionOffering> findBySessionType(SessionType sessionType);

        // findByDoctorIdAndPracticeIdAndSessionType_Id method removed - no longer
        // needed after Practice entity removal
        Optional<SessionOffering> findByDoctorIdAndSessionType_Id(UUID doctorId, UUID sessionTypeId);

        // Methods for branch transfer functionality
        List<SessionOffering> findByBranchId(UUID branchId);

        List<SessionOffering> findByBranchIdAndCreatedAtBetween(UUID branchId, java.time.ZonedDateTime startDate,
                        java.time.ZonedDateTime endDate);

        List<SessionOffering> findByIdIn(List<UUID> sessionIds);

        // Statistics methods
        @Query("SELECT AVG(so.price) FROM SessionOffering so")
        BigDecimal findAveragePrice();

        @Query("SELECT AVG(so.price) FROM SessionOffering so WHERE so.branchId = :branchId")
        BigDecimal findAveragePriceByBranchId(@Param("branchId") UUID branchId);

        // Soft delete methods
        List<SessionOffering> findByStatus(Status status);

        Page<SessionOffering> findByStatus(Status status, Pageable pageable);

        Optional<SessionOffering> findByIdAndStatus(UUID id, Status status);

        List<SessionOffering> findByDoctorIdAndStatus(UUID doctorId, Status status);

        List<SessionOffering> findByBranchIdAndStatus(UUID branchId, Status status);

        List<SessionOffering> findBySessionTypeAndStatus(SessionType sessionType, Status status);

        long countByStatus(Status status);

        @Query("SELECT so FROM SessionOffering so WHERE so.status != :status")
        List<SessionOffering> findByStatusNot(@Param("status") Status status);

        @Query("SELECT so FROM SessionOffering so WHERE so.status != :status")
        Page<SessionOffering> findByStatusNot(@Param("status") Status status, Pageable pageable);

        // Get distinct doctor IDs who have sessions
        @Query("SELECT DISTINCT so.doctorId FROM SessionOffering so WHERE so.status = :status")
        List<UUID> findDistinctDoctorIdsWithSessions(@Param("status") Status status);

        // Get distinct doctor IDs who have sessions by branch
        @Query("SELECT DISTINCT so.doctorId FROM SessionOffering so WHERE so.branchId = :branchId AND so.status = :status")
        List<UUID> findDistinctDoctorIdsWithSessionsByBranch(@Param("branchId") UUID branchId,
                        @Param("status") Status status);

        // Default methods for active sessions
        default List<UUID> findDistinctDoctorIdsWithSessions() {
                return findDistinctDoctorIdsWithSessions(Status.ACTIVE);
        }

        default List<UUID> findDistinctDoctorIdsWithSessionsByBranch(UUID branchId) {
                return findDistinctDoctorIdsWithSessionsByBranch(branchId, Status.ACTIVE);
        }
}
package com.tinysteps.sessionservice.repository;

import com.tinysteps.sessionservice.entity.SessionOffering;
import com.tinysteps.sessionservice.entity.SessionType;
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
}
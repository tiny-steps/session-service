package com.tinysteps.sessionservice.service;

import com.tinysteps.sessionservice.entity.SessionOffering;
import com.tinysteps.sessionservice.dto.SessionOfferingCreateDto;
import com.tinysteps.sessionservice.integration.constants.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface SessionOfferingService {

        SessionOffering create(SessionOfferingCreateDto createDto);

        List<SessionOffering> bulkCreateOrUpdate(List<SessionOffering> offerings);

        Optional<SessionOffering> getById(UUID id);

        Page<SessionOffering> search(
                        UUID sessionTypeId,
                        Boolean isActive,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        UUID branchId,
                        Pageable pageable);

        // Branch-based search method
        Page<SessionOffering> searchForCurrentUserBranch(
                        UUID sessionTypeId,
                        Boolean isActive,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        Pageable pageable);

        List<SessionOffering> getByDoctorId(UUID doctorId);

        // getByPracticeId method removed - no longer needed after Practice entity
        // removal

        SessionOffering update(UUID id, SessionOffering offering);

        void delete(UUID id);

        SessionOffering activate(UUID id);

        SessionOffering deactivate(UUID id);

        // Branch statistics methods
        Map<String, Object> getBranchStatistics(UUID branchId);

        Map<String, Object> getCurrentUserBranchStatistics();

        Map<String, Object> getAllBranchesStatistics();

        // Soft delete methods
        SessionOffering softDelete(UUID id);

        SessionOffering reactivate(UUID id);

        List<SessionOffering> findActiveOfferings();

        Page<SessionOffering> findActiveOfferings(Pageable pageable);

        List<SessionOffering> findDeletedOfferings();

        Page<SessionOffering> findDeletedOfferings(Pageable pageable);

        List<SessionOffering> findOfferingsByStatus(Status status);

        Page<SessionOffering> findOfferingsByStatus(Status status, Pageable pageable);

        // Methods to get doctors with sessions
        List<UUID> getDoctorIdsWithSessions();

        List<UUID> getDoctorIdsWithSessionsByBranch(UUID branchId);
}
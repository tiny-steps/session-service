package com.tinysteps.sessionservice.repository;

import com.tinysteps.sessionservice.entity.SessionType;
import com.tinysteps.sessionservice.integration.constants.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionTypeRepository
        extends JpaRepository<SessionType, UUID>,
        JpaSpecificationExecutor<SessionType> {

    Optional<SessionType> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    // Soft delete methods
    List<SessionType> findByStatus(Status status);
    Page<SessionType> findByStatus(Status status, Pageable pageable);
    
    Optional<SessionType> findByIdAndStatus(UUID id, Status status);
    Optional<SessionType> findByNameIgnoreCaseAndStatus(String name, Status status);
    
    long countByStatus(Status status);
    
    @Query("SELECT st FROM SessionType st WHERE st.status != :status")
    List<SessionType> findByStatusNot(@Param("status") Status status);
    
    @Query("SELECT st FROM SessionType st WHERE st.status != :status")
    Page<SessionType> findByStatusNot(@Param("status") Status status, Pageable pageable);
}

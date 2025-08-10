package com.tinysteps.sessionservice.repository;


import com.tinysteps.sessionservice.entity.SessionOffering;
import com.tinysteps.sessionservice.entity.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionOfferingRepository
        extends JpaRepository<SessionOffering, UUID>,
        JpaSpecificationExecutor<SessionOffering> {

    List<SessionOffering> findByDoctorId(UUID doctorId);
    List<SessionOffering> findByPracticeId(UUID practiceId);
    List<SessionOffering> findByDoctorIdAndPracticeId(UUID doctorId, UUID practiceId);
    List<SessionOffering> findBySessionType(SessionType sessionType);
    Optional<SessionOffering> findByDoctorIdAndPracticeIdAndSessionType_Id(UUID doctorId, UUID practiceId, UUID sessionTypeId);
}

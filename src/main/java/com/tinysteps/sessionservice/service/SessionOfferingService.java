package com.tinysteps.sessionservice.service;

import com.tinysteps.sessionservice.entity.SessionOffering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionOfferingService {

    SessionOffering create(SessionOffering offering);

    List<SessionOffering> bulkCreateOrUpdate(List<SessionOffering> offerings);

    Optional<SessionOffering> getById(UUID id);

    Page<SessionOffering> search(UUID doctorId,
                                       UUID practiceId,
                                       UUID sessionTypeId,
                                       Boolean isActive,
                                       BigDecimal minPrice,
                                       BigDecimal maxPrice,
                                       Pageable pageable);

    List<SessionOffering> getByDoctorId(UUID doctorId);

    List<SessionOffering> getByPracticeId(UUID practiceId);

    SessionOffering update(UUID id, SessionOffering offering);

    void delete(UUID id);

    SessionOffering activate(UUID id);

    SessionOffering deactivate(UUID id);
}

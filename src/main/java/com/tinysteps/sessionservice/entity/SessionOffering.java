package com.tinysteps.sessionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.ZonedDateTime;

/**
 * A doctor's mapping to a session type at a specific practice with price.
 */
@Entity
@Table(name = "doctor_session_offerings", uniqueConstraints = {
        @UniqueConstraint(name = "uq_doctor_session_type",
                columnNames = {"doctor_id", "session_type_id"})
}, indexes = {
        @Index(name = "idx_dso_doctor_id", columnList = "doctor_id"),
        @Index(name = "idx_dso_session_type_id", columnList = "session_type_id")
        // practice_id index removed - no longer needed after Practice entity removal
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "doctor_id")
    private UUID doctorId;

    // practiceId field removed - no longer needed after Practice entity removal
    
    @Column(name = "branch_id")
    private UUID branchId; // Branch context for multi-branch support

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_type_id", nullable = false)
    private SessionType sessionType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    private ZonedDateTime updatedAt;
}
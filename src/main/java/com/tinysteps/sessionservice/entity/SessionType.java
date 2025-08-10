package com.tinysteps.sessionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.UUID;
import java.time.ZonedDateTime;

/**
 * Catalog of possible consultation types (global across system).
 */
@Entity
@Table(name = "session_types", indexes = {
        @Index(name = "idx_session_types_name", columnList = "name", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_duration_minutes", nullable = false)
    private int defaultDurationMinutes;

    @Column(name = "is_telemedicine_available", nullable = false)
    private boolean isTelemedicineAvailable = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    private ZonedDateTime updatedAt;
}

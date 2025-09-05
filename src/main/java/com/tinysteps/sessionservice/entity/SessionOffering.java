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
@Table(name = "session_offerings", uniqueConstraints = {
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

    // Manual getters and setters for compilation
    public UUID getBranchId() {
        return branchId;
    }

    public void setBranchId(UUID branchId) {
        this.branchId = branchId;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(UUID doctorId) {
        this.doctorId = doctorId;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public static SessionOfferingBuilder builder() {
        return new SessionOfferingBuilder();
    }

    public static class SessionOfferingBuilder {
        private UUID id;
        private UUID doctorId;
        private UUID branchId;
        private SessionType sessionType;
        private BigDecimal price;
        private boolean isActive = true;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        public SessionOfferingBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public SessionOfferingBuilder doctorId(UUID doctorId) {
            this.doctorId = doctorId;
            return this;
        }

        public SessionOfferingBuilder branchId(UUID branchId) {
            this.branchId = branchId;
            return this;
        }

        public SessionOfferingBuilder sessionType(SessionType sessionType) {
            this.sessionType = sessionType;
            return this;
        }

        public SessionOfferingBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public SessionOfferingBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public SessionOfferingBuilder createdAt(ZonedDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SessionOfferingBuilder updatedAt(ZonedDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public SessionOffering build() {
            SessionOffering offering = new SessionOffering();
            offering.id = this.id;
            offering.doctorId = this.doctorId;
            offering.branchId = this.branchId;
            offering.sessionType = this.sessionType;
            offering.price = this.price;
            offering.isActive = this.isActive;
            offering.createdAt = this.createdAt;
            offering.updatedAt = this.updatedAt;
            return offering;
        }
    }
}
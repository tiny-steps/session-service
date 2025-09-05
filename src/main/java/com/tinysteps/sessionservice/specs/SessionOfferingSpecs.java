package com.tinysteps.sessionservice.specs;

import com.tinysteps.sessionservice.entity.SessionOffering;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class SessionOfferingSpecs {

    public static Specification<SessionOffering> byDoctorId(UUID doctorId) {
        return (root, cq, cb) ->
                doctorId == null ? null : cb.equal(root.get("doctorId"), doctorId);
    }

    // byPracticeId method removed - no longer needed after Practice entity removal

    public static Specification<SessionOffering> bySessionTypeId(UUID sessionTypeId) {
        return (root, cq, cb) ->
                sessionTypeId == null ? null : cb.equal(root.get("sessionType").get("id"), sessionTypeId);
    }

    public static Specification<SessionOffering> isActive(Boolean active) {
        return (root, cq, cb) ->
                active == null ? null : cb.equal(root.get("isActive"), active);
    }

    public static Specification<SessionOffering> byPriceRange(BigDecimal min, BigDecimal max) {
        return (root, cq, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) {
                return cb.between(root.get("price"), min, max);
            } else if (min != null) {
                return cb.ge(root.get("price"), min);
            } else {
                return cb.le(root.get("price"), max);
            }
        };
    }

    public static Specification<SessionOffering> byBranchId(UUID branchId) {
        return (root, cq, cb) ->
                branchId == null ? null : cb.equal(root.get("branchId"), branchId);
    }

    // More filters as needed
}

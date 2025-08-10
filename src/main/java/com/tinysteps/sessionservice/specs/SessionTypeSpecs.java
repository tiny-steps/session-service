package com.tinysteps.sessionservice.specs;


import com.tinysteps.sessionservice.entity.SessionType;
import org.springframework.data.jpa.domain.Specification;

public class SessionTypeSpecs {

    public static Specification<SessionType> byNameContains(String query) {
        return (root, cq, cb) ->
                (query == null || query.isEmpty()) ? null
                        : cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%");
    }

    public static Specification<SessionType> isActive(Boolean active) {
        return (root, cq, cb) ->
                (active == null) ? null
                        : cb.equal(root.get("isActive"), active);
    }

    public static Specification<SessionType> isTelemedicineAvailable(Boolean telemedicine) {
        return (root, cq, cb) ->
                (telemedicine == null) ? null
                        : cb.equal(root.get("isTelemedicineAvailable"), telemedicine);
    }

    public static Specification<SessionType> byDurationRange(Integer minMinutes, Integer maxMinutes) {
        return (root, cq, cb) -> {
            if (minMinutes == null && maxMinutes == null) return null;
            if (minMinutes != null && maxMinutes != null) {
                return cb.between(root.get("defaultDurationMinutes"), minMinutes, maxMinutes);
            } else if (minMinutes != null) {
                return cb.ge(root.get("defaultDurationMinutes"), minMinutes);
            } else {
                return cb.le(root.get("defaultDurationMinutes"), maxMinutes);
            }
        };
    }

    // Add more filters as needed
}


package com.tinysteps.sessionservice.config;

import com.tinysteps.sessionservice.entity.SessionOffering;
// AddressIntegrationService import removed - no longer needed after Practice entity removal
import com.tinysteps.sessionservice.integration.service.DoctorIntegrationService;
import com.tinysteps.sessionservice.repository.SessionOfferingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("sessionSecurity")
@RequiredArgsConstructor
@Slf4j
public class ApplicationSecurityConfig {

    private final SessionOfferingRepository offeringRepository;
    private final DoctorIntegrationService doctorIntegrationService;
    // AddressIntegrationService dependency removed - no longer needed after Practice entity removal

    // ---------- ROLE CHECKS ----------
    public boolean isAdmin(Authentication auth) {
        return hasRole(auth, "ADMIN");
    }
    public boolean isDoctor(Authentication auth) {
        return hasRole(auth, "DOCTOR");
    }
    public boolean isPatient(Authentication auth) {
        return hasRole(auth, "PATIENT");
    }
    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities() != null &&
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(a -> a.equals("ROLE_" + role) || a.equals(role));
    }
    private String getAuthenticatedUserId(Authentication authentication) {
        return (authentication != null) ? authentication.getName() : null;
    }

    // ---------- OWNERSHIP CHECKS ----------
    public boolean isDoctorOwner(Authentication authentication, UUID doctorId) {
        if (isAdmin(authentication)) return true;
        if (!isDoctor(authentication)) return false;
        try {
            return doctorIntegrationService.validateDoctorOwnership(doctorId, getAuthenticatedUserId(authentication));
        } catch (Exception e) {
            log.warn("Doctor ownership check failed for {}", doctorId, e);
            return false;
        }
    }

    public boolean isOfferingOwner(Authentication authentication, UUID offeringId) {
        if (isAdmin(authentication)) return true;
        Optional<SessionOffering> off = offeringRepository.findById(offeringId);
        return off.filter(o -> isDoctorOwner(authentication, o.getDoctorId())).isPresent();
    }

    // isPracticeOwner method removed - no longer needed after Practice entity removal
}

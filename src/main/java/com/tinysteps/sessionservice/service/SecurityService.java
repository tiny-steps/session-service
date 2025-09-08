package com.tinysteps.sessionservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Enhanced SecurityService with comprehensive defensive programming patterns.
 * Provides secure access to JWT token claims with robust validation and error
 * handling.
 */
@Service
@Slf4j
public class SecurityService {

    private static final Set<String> VALID_DOMAIN_TYPES = Set.of(
            "healthcare", "ecommerce", "cab-booking", "payment", "financial");

    /**
     * Get the currently authenticated user's ID from JWT token claims with enhanced
     * validation.
     *
     * @return User ID from token claims (never null or empty)
     * @throws SecurityException if user is not authenticated or ID claim is
     *                           missing/invalid
     */
    public String getCurrentUserId() {
        log.debug("Retrieving current user ID from JWT token");

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                log.warn("No authentication context found");
                throw new SecurityException("Authentication context not found");
            }

            if (!auth.isAuthenticated()) {
                log.warn("User is not authenticated");
                throw new SecurityException("User is not authenticated");
            }

            if (!(auth.getPrincipal() instanceof Jwt jwt)) {
                log.warn("Authentication principal is not a JWT token");
                throw new SecurityException("Invalid authentication token type");
            }

            String userId = jwt.getClaimAsString("id");
            if (!StringUtils.hasText(userId)) {
                log.warn("User ID claim is missing or empty in JWT token");
                throw new SecurityException("User ID not found in token claims");
            }

            // Validate user ID format (basic validation)
            if (userId.trim().length() < 1) {
                log.warn("User ID claim contains only whitespace");
                throw new SecurityException("Invalid user ID format");
            }

            log.debug("Successfully retrieved user ID: {}", userId);
            return userId.trim();

        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format: {}", e.getMessage());
            throw new SecurityException("Invalid user ID format", e);
        } catch (SecurityException e) {
            // Re-throw security exceptions as-is
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error retrieving user ID: {}", e.getMessage());
            throw new SecurityException("Failed to retrieve user ID", e);
        }
    }

    /**
     * Get the currently authenticated user's roles from JWT token claims with
     * enhanced validation.
     *
     * @return List of user roles (never null, empty list if no roles found)
     * @throws SecurityException if authentication context is invalid
     */
    public List<String> getCurrentUserRoles() {
        log.debug("Retrieving current user roles from JWT token");

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                log.debug("No authentication context found, returning empty roles list");
                return List.of();
            }

            if (!auth.isAuthenticated()) {
                log.debug("User is not authenticated, returning empty roles list");
                return List.of();
            }

            if (!(auth.getPrincipal() instanceof Jwt jwt)) {
                log.debug("Authentication principal is not a JWT token, returning empty roles list");
                return List.of();
            }

            List<String> roles;
            Object roleClaim = jwt.getClaim("role");

            if (roleClaim instanceof String roleSingle) {
                // Handle single role as string
                roles = StringUtils.hasText(roleSingle) ? List.of(roleSingle) : List.of();
            } else if (roleClaim instanceof List) {
                // Handle roles as list
                roles = jwt.getClaimAsStringList("role");
            } else {
                log.debug("No valid roles found in JWT token");
                return List.of();
            }

            if (roles == null || roles.isEmpty()) {
                log.debug("No roles found in JWT token");
                return List.of();
            }

            // Filter out null, empty, or whitespace-only roles
            List<String> validRoles = roles.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(Collectors.toList());

            log.debug("Successfully retrieved {} valid roles", validRoles.size());
            return validRoles;

        } catch (Exception e) {
            log.error("Unexpected error retrieving user roles: {}", e.getMessage());
            return List.of(); // Fail-safe: return empty list instead of throwing
        }
    }

    /**
     * Check if the current user has a specific role
     *
     * @param role Role to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        try {
            List<String> roles = getCurrentUserRoles();
            return roles != null && roles.contains(role);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the current user is an admin
     *
     * @return true if user has ADMIN role, false otherwise
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if the current user can access resources for the given user ID
     * Users can access their own resources, admins can access any resources
     *
     * @param targetUserId The user ID being accessed
     * @return true if access is allowed, false otherwise
     */
    public boolean canAccessUserResources(String targetUserId) {
        try {
            String currentUserId = getCurrentUserId();

            // Users can access their own resources
            if (currentUserId.equals(targetUserId)) {
                return true;
            }

            // Admins can access any resources
            return isAdmin();

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate that the current user can access resources for the given user ID
     * Throws exception if access is not allowed
     *
     * @param targetUserId The user ID being accessed
     * @throws RuntimeException if access is not allowed
     */
    public void validateUserAccess(String targetUserId) {
        if (!canAccessUserResources(targetUserId)) {
            throw new RuntimeException("Access denied: You can only access your own resources");
        }
    }

    /**
     * Get the currently authenticated user's context IDs for a specific domain from
     * JWT token claims
     *
     * @param domainType The domain type (e.g., "healthcare", "ecommerce",
     *                   "cab-booking")
     * @return List of context IDs (as UUIDs) from token claims
     */
    /**
     * Get the currently authenticated user's context IDs for a specific domain from
     * JWT token claims with enhanced validation.
     *
     * @param domainType The domain type (e.g., "healthcare", "ecommerce",
     *                   "cab-booking")
     * @return List of context IDs (as UUIDs) from token claims (never null, empty
     *         list if none found)
     * @throws SecurityException if authentication context is invalid or domain type
     *                           is invalid
     */
    public List<UUID> getContextIds(String domainType) {
        log.debug("Retrieving context IDs for domain type: {}", domainType);

        // Input validation
        if (!StringUtils.hasText(domainType)) {
            log.warn("Domain type is null or empty");
            throw new SecurityException("Domain type cannot be null or empty");
        }

        if (!VALID_DOMAIN_TYPES.contains(domainType.trim())) {
            log.warn("Invalid domain type provided: {}", domainType);
            throw new SecurityException("Invalid domain type: " + domainType);
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                log.debug("No authentication context found");
                throw new SecurityException("User is not authenticated");
            }

            if (!auth.isAuthenticated()) {
                log.debug("User is not authenticated");
                throw new SecurityException("User is not authenticated");
            }

            if (!(auth.getPrincipal() instanceof Jwt jwt)) {
                log.debug("Authentication principal is not a JWT token");
                throw new SecurityException("Invalid authentication token");
            }

            List<String> contextIdStrings = jwt.getClaimAsStringList("contextIds");  // Changed from "context_ids" to "contextIds"
            String tokenDomainType = jwt.getClaimAsString("domainType"); // Changed from "domain_type" to "domainType"

            if (contextIdStrings == null || contextIdStrings.isEmpty()) {
                log.debug("No context IDs found in JWT token for domain: {}", domainType);
                return List.of();
            }

            if (!domainType.equals(tokenDomainType)) {
                log.debug("Domain type mismatch - requested: {}, token: {}", domainType, tokenDomainType);
                return List.of();
            }

            // Validate and convert context ID strings to UUIDs
            List<UUID> contextIds = contextIdStrings.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .map(contextIdStr -> {
                        try {
                            return UUID.fromString(contextIdStr);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid UUID format for context ID: {}", contextIdStr);
                            return null;
                        }
                    })
                    .filter(uuid -> uuid != null)
                    .collect(Collectors.toList());

            log.debug("Successfully retrieved {} valid context IDs for domain: {}", contextIds.size(), domainType);
            return contextIds;

        } catch (SecurityException e) {
            throw e; // Re-throw security exceptions
        } catch (Exception e) {
            log.error("Unexpected error retrieving context IDs for domain {}: {}", domainType, e.getMessage());
            throw new SecurityException("Failed to retrieve context IDs: " + e.getMessage());
        }
    }

    /**
     * Get the currently authenticated user's primary context ID for a specific
     * domain from JWT token claims with enhanced validation.
     *
     * @param domainType The domain type (e.g., "healthcare", "ecommerce",
     *                   "cab-booking")
     * @return Primary context ID (as UUID) from token claims, or null if not set
     * @throws SecurityException if authentication context is invalid or domain type
     *                           is invalid
     */
    public UUID getPrimaryContextId(String domainType) {
        log.debug("Retrieving primary context ID for domain type: {}", domainType);

        // Input validation
        if (!StringUtils.hasText(domainType)) {
            log.warn("Domain type is null or empty");
            throw new SecurityException("Domain type cannot be null or empty");
        }

        if (!VALID_DOMAIN_TYPES.contains(domainType.trim())) {
            log.warn("Invalid domain type provided: {}", domainType);
            throw new SecurityException("Invalid domain type: " + domainType);
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                log.debug("No authentication context found");
                throw new SecurityException("User is not authenticated");
            }

            if (!auth.isAuthenticated()) {
                log.debug("User is not authenticated");
                throw new SecurityException("User is not authenticated");
            }

            if (!(auth.getPrincipal() instanceof Jwt jwt)) {
                log.debug("Authentication principal is not a JWT token");
                throw new SecurityException("Invalid authentication token");
            }

            String primaryContextIdString = jwt.getClaimAsString("primaryContextId");  // Changed from "primary_context_id" to "primaryContextId"
            String tokenDomainType = jwt.getClaimAsString("domainType"); // Changed from "domain_type" to "domainType"

            if (!StringUtils.hasText(primaryContextIdString)) {
                // Fallback to legacy primary_branch_id for backward compatibility
                primaryContextIdString = jwt.getClaimAsString("primaryBranchId");
                if (!StringUtils.hasText(primaryContextIdString)) {
                    log.debug("No primary context ID found in JWT token for domain: {}", domainType);
                    return null;
                }
            }

            if (!domainType.equals(tokenDomainType)) {
                log.debug("Domain type mismatch - requested: {}, token: {}", domainType, tokenDomainType);
                return null;
            }

            // Validate UUID format
            try {
                UUID primaryContextId = UUID.fromString(primaryContextIdString.trim());
                log.debug("Successfully retrieved primary context ID for domain: {}", domainType);
                return primaryContextId;
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID format for primary context ID: {}", primaryContextIdString);
                return null;
            }

        } catch (SecurityException e) {
            throw e; // Re-throw security exceptions
        } catch (Exception e) {
            log.error("Unexpected error retrieving primary context ID for domain {}: {}", domainType, e.getMessage());
            throw new SecurityException("Failed to retrieve primary context ID: " + e.getMessage());
        }
    }

    /**
     * Get the currently authenticated user's branch IDs from JWT token claims
     * 
     * @deprecated Use getContextIds("healthcare") instead
     * @return List of branch IDs (as UUIDs) from token claims
     */
    @Deprecated
    public List<UUID> getBranchIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            // Try new context-based claims first
            try {
                List<UUID> contextIds = getContextIds("healthcare");
                if (!contextIds.isEmpty()) {
                    return contextIds;
                }
            } catch (Exception e) {
                // Fall back to legacy claims
            }

            // Fall back to legacy branch claims
            List<String> branchIdStrings = jwt.getClaimAsStringList("branchIds");
            if (branchIdStrings == null || branchIdStrings.isEmpty()) {
                return List.of(); // Return empty list if no branch IDs
            }

            return branchIdStrings.stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
        }

        throw new RuntimeException("Invalid authentication token");
    }

    /**
     * Get the currently authenticated user's primary branch ID from JWT token
     * claims
     *
     * @deprecated Use getPrimaryContextId("healthcare") instead
     * @return Primary branch ID (as UUID) from token claims, or null if not set
     */
    @Deprecated
    public UUID getPrimaryBranchId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            // Try new context-based claims first
            try {
                UUID contextId = getPrimaryContextId("healthcare");
                if (contextId != null) {
                    return contextId;
                }
            } catch (Exception e) {
                // Fall back to legacy claims
            }

            // Fall back to legacy branch claims
            String primaryBranchIdString = jwt.getClaimAsString("primaryBranchId");
            if (primaryBranchIdString == null || primaryBranchIdString.isEmpty()) {
                return null; // Return null if no primary branch ID
            }

            return UUID.fromString(primaryBranchIdString);
        }

        throw new RuntimeException("Invalid authentication token");
    }

    /**
     * Check if the current user has access to a specific context in a domain
     *
     * @param contextId  The context ID to check access for (as String)
     * @param domainType The domain type (e.g., "healthcare", "ecommerce",
     *                   "cab-booking")
     * @return true if user has access, false otherwise
     */
    /**
     * Check if the current user has access to a specific context in a domain with
     * enhanced validation.
     *
     * @param contextId  The context ID to check access for (as String)
     * @param domainType The domain type (e.g., "healthcare", "ecommerce",
     *                   "cab-booking")
     * @return true if user has access, false otherwise (fail-safe)
     */
    public boolean hasAccessToContext(String contextId, String domainType) {
        log.debug("Checking context access for contextId: {}, domainType: {}", contextId, domainType);

        // Input validation
        if (!StringUtils.hasText(contextId)) {
            log.debug("Context ID is null or empty, denying access");
            return false;
        }

        if (!StringUtils.hasText(domainType)) {
            log.debug("Domain type is null or empty, denying access");
            return false;
        }

        if (!VALID_DOMAIN_TYPES.contains(domainType.trim())) {
            log.debug("Invalid domain type: {}, denying access", domainType);
            return false;
        }

        try {
            UUID contextUuid = UUID.fromString(contextId.trim());
            boolean hasAccess = hasAccessToContext(contextUuid, domainType);
            log.debug("Context access check result for {}: {}", contextId, hasAccess);
            return hasAccess;
        } catch (IllegalArgumentException e) {
            log.debug("Invalid UUID format for context ID: {}, denying access", contextId);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error checking context access for {}: {}", contextId, e.getMessage());
            return false; // Fail-safe: deny access on error
        }
    }

    /**
     * Check if the current user has access to a specific context in a domain with
     * enhanced validation.
     *
     * @param contextId  The context ID to check access for (as UUID)
     * @param domainType The domain type (e.g., "healthcare", "ecommerce",
     *                   "cab-booking")
     * @return true if user has access, false otherwise (fail-safe)
     */
    public boolean hasAccessToContext(UUID contextId, String domainType) {
        log.debug("Checking context access for contextId: {}, domainType: {}", contextId, domainType);

        // Input validation
        if (contextId == null) {
            log.debug("Context ID is null, denying access");
            return false;
        }

        if (!StringUtils.hasText(domainType)) {
            log.debug("Domain type is null or empty, denying access");
            return false;
        }

        if (!VALID_DOMAIN_TYPES.contains(domainType.trim())) {
            log.debug("Invalid domain type: {}, denying access", domainType);
            return false;
        }

        try {
            List<UUID> userContextIds = getContextIds(domainType);
            boolean hasAccess = userContextIds.contains(contextId);
            log.debug("Context access check result for {}: {}", contextId, hasAccess);
            return hasAccess;
        } catch (Exception e) {
            log.error("Unexpected error checking context access for {}: {}", contextId, e.getMessage());
            return false; // Fail-safe: deny access on error
        }
    }

    /**
     * Validate that the current user has access to a specific context in a domain
     * Throws exception if access is denied
     *
     * @param contextId  The context ID to validate access for (as String)
     * @param domainType The domain type (e.g., "healthcare", "ecommerce",
     *                   "cab-booking")
     * @throws RuntimeException if user doesn't have access to the context
     */
    public void validateContextAccess(String contextId, String domainType) {
        if (!hasAccessToContext(contextId, domainType)) {
            throw new RuntimeException(
                    "Access denied: User does not have access to context " + contextId + " in domain " + domainType);
        }
    }

    /**
     * Validate that the current user has access to a specific context in a domain
     * Throws exception if access is denied
     *
     * @param contextId  The context ID to validate access for (as UUID)
     * @param domainType The domain type (e.g., "healthcare", "ecommerce",
     *                   "cab-booking")
     * @throws RuntimeException if user doesn't have access to the context
     */
    public void validateContextAccess(UUID contextId, String domainType) {
        if (!hasAccessToContext(contextId, domainType)) {
            throw new RuntimeException(
                    "Access denied: User does not have access to context " + contextId + " in domain " + domainType);
        }
    }

    /**
     * Check if the current user has access to a specific branch
     *
     * @deprecated Use hasAccessToContext(branchId, "healthcare") instead
     * @param branchId The branch ID to check access for
     * @return true if user has access to the branch, false otherwise
     */
    @Deprecated
    public boolean hasBranchAccess(String branchId) {
        if (branchId == null || branchId.isEmpty()) {
            return false;
        }

        try {
            UUID branchUuid = UUID.fromString(branchId);
            return hasBranchAccess(branchUuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if the current user has access to a specific branch
     *
     * @deprecated Use hasAccessToContext(branchId, "healthcare") instead
     * @param branchId The branch ID (as UUID) to check access for
     * @return true if user has access to the branch, false otherwise
     */
    @Deprecated
    public boolean hasBranchAccess(UUID branchId) {
        if (branchId == null) {
            return false;
        }

        // Try new context-based access first
        try {
            return hasAccessToContext(branchId, "healthcare");
        } catch (Exception e) {
            // Fall back to legacy branch access
        }

        try {
            List<UUID> userBranchIds = getBranchIds();
            return userBranchIds.contains(branchId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate that the current user has access to a specific branch
     * Throws exception if access is not allowed
     *
     * @deprecated Use validateContextAccess(branchId, "healthcare") instead
     * @param branchId The branch ID to validate access for
     * @throws RuntimeException if access is not allowed
     */
    @Deprecated
    public void validateBranchAccess(String branchId) {
        if (!hasBranchAccess(branchId)) {
            throw new RuntimeException("Access denied: You do not have access to this branch");
        }
    }

    /**
     * Validate that the current user has access to a specific branch
     * Throws exception if access is not allowed
     *
     * @deprecated Use validateContextAccess(branchId, "healthcare") instead
     * @param branchId The branch ID (as UUID) to validate access for
     * @throws RuntimeException if access is not allowed
     */
    @Deprecated
    public void validateBranchAccess(UUID branchId) {
        if (!hasBranchAccess(branchId)) {
            throw new RuntimeException("Access denied: You do not have access to this branch");
        }
    }
}

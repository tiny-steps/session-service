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
 * Provides secure access to JWT token claims with robust validation and error handling.
 */
@Service
@Slf4j
public class SecurityService {
    
    private static final Set<String> VALID_DOMAIN_TYPES = Set.of(
        "healthcare", "ecommerce", "cab-booking", "payment", "financial"
    );

    /**
     * Get the currently authenticated user's ID from JWT token claims
     * Enhanced with comprehensive defensive programming patterns
     * @return User ID from token claims
     * @throws RuntimeException if user is not authenticated or ID claim is missing
     */
    public String getCurrentUserId() {
        log.debug("Attempting to retrieve current user ID from JWT token");
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null) {
                log.warn("No authentication context found");
                throw new RuntimeException("User is not authenticated - no security context");
            }
            
            if (!authentication.isAuthenticated()) {
                log.warn("User authentication is not valid");
                throw new RuntimeException("User is not authenticated - invalid authentication state");
            }

            if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
                log.error("Authentication principal is not a JWT token: {}", 
                    authentication.getPrincipal().getClass().getSimpleName());
                throw new RuntimeException("Invalid authentication token - not a JWT");
            }
            
            String userId = jwt.getClaimAsString("id");
            if (!StringUtils.hasText(userId)) {
                log.error("User ID claim is null or empty in JWT token");
                throw new RuntimeException("User ID not found in token claims");
            }
            
            // Validate UUID format
            try {
                UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format for user ID: {}", userId);
                throw new RuntimeException("Invalid user ID format in token claims");
            }
            
            log.debug("Successfully retrieved user ID: {}", userId);
            return userId;
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument while retrieving user ID: {}", e.getMessage());
            throw new RuntimeException("Invalid user ID format: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while retrieving user ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user ID: " + e.getMessage(), e);
        }
    }

    /**
     * Get the currently authenticated user's roles from JWT token claims
     * Enhanced with comprehensive defensive programming patterns
     * @return List of user roles (never null, may be empty)
     */
    public List<String> getCurrentUserRoles() {
        log.debug("Attempting to retrieve current user roles from JWT token");
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null) {
                log.warn("No authentication context found while retrieving roles");
                throw new RuntimeException("User is not authenticated - no security context");
            }
            
            if (!authentication.isAuthenticated()) {
                log.warn("User authentication is not valid while retrieving roles");
                throw new RuntimeException("User is not authenticated - invalid authentication state");
            }

            if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
                log.error("Authentication principal is not a JWT token while retrieving roles: {}", 
                    authentication.getPrincipal().getClass().getSimpleName());
                throw new RuntimeException("Invalid authentication token - not a JWT");
            }
            
            List<String> roles = jwt.getClaimAsStringList("role");
            if (roles == null) {
                log.debug("No roles claim found in JWT token, returning empty list");
                return List.of();
            }
            
            // Filter out null or empty roles
            List<String> validRoles = roles.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
            
            log.debug("Successfully retrieved {} valid roles from JWT token", validRoles.size());
            return validRoles;
            
        } catch (Exception e) {
            log.error("Unexpected error while retrieving user roles: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user roles: " + e.getMessage(), e);
        }
    }

    /**
     * Check if the current user has a specific role
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
     * @return true if user has ADMIN role, false otherwise
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if the current user can access resources for the given user ID
     * Users can access their own resources, admins can access any resources
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
     * @param targetUserId The user ID being accessed
     * @throws RuntimeException if access is not allowed
     */
    public void validateUserAccess(String targetUserId) {
        if (!canAccessUserResources(targetUserId)) {
            throw new RuntimeException("Access denied: You can only access your own resources");
        }
    }

    /**
     * Get the currently authenticated user's context IDs for a specific domain from JWT token claims
     * @param domainType The domain type (e.g., "healthcare", "ecommerce", "cab-booking")
     * @return List of context IDs (as UUIDs) from token claims
     */
    /**
     * Get the currently authenticated user's context IDs for a specific domain from JWT token claims
     * Enhanced with comprehensive defensive programming patterns
     * @param domainType The domain type (e.g., "healthcare", "ecommerce", "cab-booking")
     * @return List of context IDs (as UUIDs) from token claims (never null, may be empty)
     * @throws RuntimeException if user is not authenticated or domain type is invalid
     */
    public List<UUID> getContextIds(String domainType) {
        // Input validation
        if (!StringUtils.hasText(domainType)) {
            log.error("Domain type is null or empty");
            throw new RuntimeException("Domain type cannot be null or empty");
        }
        
        if (!VALID_DOMAIN_TYPES.contains(domainType)) {
            log.error("Invalid domain type provided: {}", domainType);
            throw new RuntimeException("Invalid domain type: " + domainType);
        }
        
        log.debug("Attempting to retrieve context IDs for domain type: {}", domainType);
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null) {
                log.warn("No authentication context found while retrieving context IDs");
                throw new RuntimeException("User is not authenticated - no security context");
            }
            
            if (!authentication.isAuthenticated()) {
                log.warn("User authentication is not valid while retrieving context IDs");
                throw new RuntimeException("User is not authenticated - invalid authentication state");
            }

            if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
                log.error("Authentication principal is not a JWT token while retrieving context IDs: {}", 
                    authentication.getPrincipal().getClass().getSimpleName());
                throw new RuntimeException("Invalid authentication token - not a JWT");
            }
            
            List<String> contextIdStrings = jwt.getClaimAsStringList("context_ids");
            String tokenDomainType = jwt.getClaimAsString("domain_type");
            
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
            
        } catch (Exception e) {
            log.error("Unexpected error while retrieving context IDs for domain {}: {}", domainType, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve context IDs: " + e.getMessage(), e);
        }
    }

    /**
     * Get the currently authenticated user's primary context ID for a specific domain from JWT token claims
     * Enhanced with comprehensive defensive programming patterns
     * @param domainType The domain type (e.g., "healthcare", "ecommerce", "cab-booking")
     * @return Primary context ID (as UUID) from token claims, or null if not set
     * @throws RuntimeException if user is not authenticated or domain type is invalid
     */
    public UUID getPrimaryContextId(String domainType) {
        // Input validation
        if (!StringUtils.hasText(domainType)) {
            log.error("Domain type is null or empty");
            throw new RuntimeException("Domain type cannot be null or empty");
        }
        
        if (!VALID_DOMAIN_TYPES.contains(domainType)) {
            log.error("Invalid domain type provided: {}", domainType);
            throw new RuntimeException("Invalid domain type: " + domainType);
        }
        
        log.debug("Attempting to retrieve primary context ID for domain type: {}", domainType);
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null) {
                log.warn("No authentication context found while retrieving primary context ID");
                throw new RuntimeException("User is not authenticated - no security context");
            }
            
            if (!authentication.isAuthenticated()) {
                log.warn("User authentication is not valid while retrieving primary context ID");
                throw new RuntimeException("User is not authenticated - invalid authentication state");
            }

            if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
                log.error("Authentication principal is not a JWT token while retrieving primary context ID: {}", 
                    authentication.getPrincipal().getClass().getSimpleName());
                throw new RuntimeException("Invalid authentication token - not a JWT");
            }
            
            String primaryContextIdString = jwt.getClaimAsString("primary_context_id");
            String tokenDomainType = jwt.getClaimAsString("domain_type");
            
            if (!StringUtils.hasText(primaryContextIdString)) {
                // Fallback to legacy claim for backward compatibility
                primaryContextIdString = jwt.getClaimAsString("primary_branch_id");
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
                UUID primaryContextId = UUID.fromString(primaryContextIdString);
                log.debug("Successfully retrieved primary context ID: {} for domain: {}", primaryContextId, domainType);
                return primaryContextId;
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format for primary context ID: {}", primaryContextIdString);
                throw new RuntimeException("Invalid primary context ID format in token claims");
            }
            
        } catch (Exception e) {
            log.error("Unexpected error while retrieving primary context ID for domain {}: {}", domainType, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve primary context ID: " + e.getMessage(), e);
        }
    }

    /**
     * Get the currently authenticated user's branch IDs from JWT token claims
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
     * Get the currently authenticated user's primary branch ID from JWT token claims
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
     * @param contextId The context ID to check access for (as String)
     * @param domainType The domain type (e.g., "healthcare", "ecommerce", "cab-booking")
     * @return true if user has access, false otherwise
     */
    public boolean hasAccessToContext(String contextId, String domainType) {
        if (contextId == null || contextId.isEmpty() || domainType == null || domainType.isEmpty()) {
            return false;
        }
        
        try {
            UUID contextUuid = UUID.fromString(contextId);
            return hasAccessToContext(contextUuid, domainType);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Check if the current user has access to a specific context in a domain
     * @param contextId The context ID to check access for (as UUID)
     * @param domainType The domain type (e.g., "healthcare", "ecommerce", "cab-booking")
     * @return true if user has access, false otherwise
     */
    public boolean hasAccessToContext(UUID contextId, String domainType) {
        if (contextId == null || domainType == null || domainType.isEmpty()) {
            return false;
        }
        
        List<UUID> userContextIds = getContextIds(domainType);
        return userContextIds.contains(contextId);
    }

    /**
     * Validate that the current user has access to a specific context in a domain
     * Throws exception if access is denied
     * @param contextId The context ID to validate access for (as String)
     * @param domainType The domain type (e.g., "healthcare", "ecommerce", "cab-booking")
     * @throws RuntimeException if user doesn't have access to the context
     */
    public void validateContextAccess(String contextId, String domainType) {
        if (!hasAccessToContext(contextId, domainType)) {
            throw new RuntimeException("Access denied: User does not have access to context " + contextId + " in domain " + domainType);
        }
    }

    /**
     * Validate that the current user has access to a specific context in a domain
     * Throws exception if access is denied
     * @param contextId The context ID to validate access for (as UUID)
     * @param domainType The domain type (e.g., "healthcare", "ecommerce", "cab-booking")
     * @throws RuntimeException if user doesn't have access to the context
     */
    public void validateContextAccess(UUID contextId, String domainType) {
        if (!hasAccessToContext(contextId, domainType)) {
            throw new RuntimeException("Access denied: User does not have access to context " + contextId + " in domain " + domainType);
        }
    }

    /**
     * Check if the current user has access to a specific branch
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
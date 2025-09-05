package com.tinysteps.sessionservice.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SecurityService {

    /**
     * Get the currently authenticated user's ID from JWT token claims
     * @return User ID from token claims
     * @throws RuntimeException if user is not authenticated or ID claim is missing
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String userId = jwt.getClaimAsString("id");
            if (userId == null || userId.isEmpty()) {
                throw new RuntimeException("User ID not found in token claims");
            }
            return userId;
        }
        
        throw new RuntimeException("Invalid authentication token");
    }

    /**
     * Get the currently authenticated user's roles from JWT token claims
     * @return List of user roles
     */
    public List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsStringList("role");
        }
        
        throw new RuntimeException("Invalid authentication token");
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
    public List<UUID> getContextIds(String domainType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            List<String> contextIdStrings = jwt.getClaimAsStringList("context_ids");
            String tokenDomainType = jwt.getClaimAsString("domain_type");
            
            if (contextIdStrings == null || contextIdStrings.isEmpty() || !domainType.equals(tokenDomainType)) {
                return List.of(); // Return empty list if no context IDs or domain mismatch
            }
            
            return contextIdStrings.stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
        }
        
        throw new RuntimeException("Invalid authentication token");
    }

    /**
     * Get the currently authenticated user's primary context ID for a specific domain from JWT token claims
     * @param domainType The domain type (e.g., "healthcare", "ecommerce", "cab-booking")
     * @return Primary context ID (as UUID) from token claims, or null if not set
     */
    public UUID getPrimaryContextId(String domainType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String primaryContextIdString = jwt.getClaimAsString("primary_context_id");
            String tokenDomainType = jwt.getClaimAsString("domain_type");
            
            if (primaryContextIdString == null || primaryContextIdString.isEmpty() || !domainType.equals(tokenDomainType)) {
                return null; // Return null if no primary context ID or domain mismatch
            }
            
            return UUID.fromString(primaryContextIdString);
        }
        
        throw new RuntimeException("Invalid authentication token");
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
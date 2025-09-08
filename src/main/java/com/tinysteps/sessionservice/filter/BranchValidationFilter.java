package com.tinysteps.sessionservice.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinysteps.sessionservice.service.SecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BranchValidationFilter extends OncePerRequestFilter {

    private final SecurityService securityService;
    private final ObjectMapper objectMapper;

    // Patterns for endpoints that require branch validation
    private static final List<Pattern> BRANCH_REQUIRED_PATTERNS = List.of(
            Pattern.compile("/api/v1/session-offerings.*"),
            Pattern.compile("/api/v1/sessions.*"));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Skip validation for non-protected endpoints
        if (!requiresBranchValidation(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip validation if user is not authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Check if user has required roles
            List<String> userRoles = getUserRolesFromAuthentication(authentication);
            log.debug("User roles retrieved: {}", userRoles);
            if (!hasRequiredRole(userRoles)) {
                log.warn(
                        "User does not have required roles. User roles: {}, Required roles: ADMIN, DOCTOR, RECEPTIONIST",
                        userRoles);
                sendErrorResponse(response, HttpStatus.FORBIDDEN, "Access denied: Insufficient privileges");
                return;
            }

            // Extract branchId from request
            String branchId = extractBranchId(request);
            log.debug("Extracted branchId from request: {}", branchId);

            if (branchId != null) {
                // Validate branch access
                securityService.validateContextAccess(branchId, "healthcare");
                log.debug("Branch validation successful for branchId: {}", branchId);
            } else {
                // Use primary branch if no branchId specified
                UUID primaryBranchId = securityService.getPrimaryContextId("healthcare");
                log.debug("Primary branch ID retrieved: {}", primaryBranchId);
                if (primaryBranchId != null) {
                    request.setAttribute("branchId", primaryBranchId.toString());
                    log.debug("Using primary branch: {}", primaryBranchId);
                } else {
                    log.warn("No primary branch ID found for user");
                }
            }

            filterChain.doFilter(request, response);

        } catch (RuntimeException e) {
            log.warn("Branch validation failed: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    private boolean requiresBranchValidation(String requestURI) {
        return BRANCH_REQUIRED_PATTERNS.stream()
                .anyMatch(pattern -> pattern.matcher(requestURI).matches());
    }

    private boolean hasRequiredRole(List<String> userRoles) {
        return userRoles.contains("ADMIN") ||
                userRoles.contains("DOCTOR") ||
                userRoles.contains("RECEPTIONIST");
    }

    private List<String> getUserRolesFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> {
                    // Remove "ROLE_" prefix if present
                    if (authority.startsWith("ROLE_")) {
                        return authority.substring(5);
                    }
                    return authority;
                })
                .collect(Collectors.toList());
    }

    private String extractBranchId(HttpServletRequest request) throws IOException {
        // First, try to get from query parameters
        String branchId = request.getParameter("branchId");
        if (branchId != null && !branchId.isEmpty()) {
            return branchId;
        }

        // Then, try to extract from path variables
        branchId = extractFromPathVariable(request.getRequestURI());
        if (branchId != null) {
            return branchId;
        }

        // Finally, try to extract from request body (for POST/PUT requests)
        if ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod())) {
            return extractFromRequestBody(request);
        }

        return null;
    }

    private String extractFromPathVariable(String requestURI) {
        // Extract branchId from path patterns like /api/v1/branches/{branchId}/...
        // Also handle sessions endpoint patterns
        Pattern pattern = Pattern.compile("/branches/([a-fA-F0-9-]{36})");
        Matcher matcher = pattern.matcher(requestURI);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // For sessions endpoint, we don't extract branchId from path
        // Branch ID should come from query parameters or request body
        return null;
    }

    private String extractFromRequestBody(HttpServletRequest request) throws IOException {
        ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request);
        byte[] body = StreamUtils.copyToByteArray(wrapper.getInputStream());

        if (body.length > 0) {
            String bodyString = new String(body, StandardCharsets.UTF_8);
            try {
                JsonNode jsonNode = objectMapper.readTree(bodyString);
                JsonNode branchIdNode = jsonNode.get("branchId");
                if (branchIdNode != null && !branchIdNode.isNull()) {
                    return branchIdNode.asText();
                }
            } catch (Exception e) {
                log.debug("Could not parse request body as JSON: {}", e.getMessage());
            }
        }

        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String errorResponse = String.format(
                "{\"error\": \"%s\", \"message\": \"%s\", \"status\": %d}",
                status.getReasonPhrase(),
                message,
                status.value());

        response.getWriter().write(errorResponse);
    }
}
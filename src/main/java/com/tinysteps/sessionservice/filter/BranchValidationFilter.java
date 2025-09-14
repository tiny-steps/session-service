package com.tinysteps.sessionservice.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinysteps.sessionservice.service.SecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
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
            Pattern.compile("/api/v1/session-offerings.*"), // legacy pattern (kept for safety)
            Pattern.compile("/api/v1/sessions.*"));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        if (!requiresBranchValidation(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Handle special branchId=all (admin-wide scope) early
        String rawBranchParam = request.getParameter("branchId");
        if (rawBranchParam != null && rawBranchParam.equalsIgnoreCase("all")) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ADMIN"));
            if (!isAdmin) {
                sendErrorResponse(response, HttpStatus.FORBIDDEN, "Access denied: Only ADMIN can query all branches");
                return;
            }
            // Mark scope so downstream logic can differentiate if needed
            request.setAttribute("branchScope", "ALL");
            filterChain.doFilter(request, response);
            return; // Skip normal validation
        }

        HttpServletRequest requestToUse = request;
        byte[] cachedBody = null;

        // We need potential branchId from body BEFORE controller; read & wrap safely.
        boolean bodyCandidate = ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod()));
        if (bodyCandidate) {
            try {
                cachedBody = request.getInputStream().readAllBytes();
                requestToUse = new CachedBodyRequestWrapper(request, cachedBody);
            } catch (IOException ioe) {
                log.warn("Failed to read request body for branch validation: {}", ioe.getMessage());
            }
        }

        try {
            List<String> userRoles = getUserRolesFromAuthentication(authentication);
            log.debug("User roles retrieved: {}", userRoles);
            if (!hasRequiredRole(userRoles)) {
                log.warn("User does not have required roles. User roles: {}", userRoles);
                sendErrorResponse(response, HttpStatus.FORBIDDEN, "Access denied: Insufficient privileges");
                return;
            }

            String branchId = extractBranchId(requestToUse, cachedBody);
            log.debug("Extracted branchId from request: {}", branchId);

            if (branchId != null) {
                securityService.validateContextAccess(branchId, "healthcare");
                log.debug("Branch validation successful for branchId: {}", branchId);
            } else {
                UUID primaryBranchId = securityService.getPrimaryContextId("healthcare");
                log.debug("Primary branch ID retrieved: {}", primaryBranchId);
                if (primaryBranchId != null) {
                    requestToUse.setAttribute("branchId", primaryBranchId.toString());
                    log.debug("Using primary branch: {}", primaryBranchId);
                } else {
                    log.warn("No primary branch ID found for user");
                }
            }

            filterChain.doFilter(requestToUse, response);
        } catch (RuntimeException e) {
            log.warn("Branch validation failed: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    private boolean requiresBranchValidation(String requestURI) {
        return BRANCH_REQUIRED_PATTERNS.stream().anyMatch(p -> p.matcher(requestURI).matches());
    }

    private boolean hasRequiredRole(List<String> userRoles) {
        return userRoles.contains("ADMIN") || userRoles.contains("DOCTOR") || userRoles.contains("RECEPTIONIST");
    }

    private List<String> getUserRolesFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
                .collect(Collectors.toList());
    }

    private String extractBranchId(HttpServletRequest request, byte[] cachedBody) {
        // Query param first
        String branchId = request.getParameter("branchId");
        if (branchId != null && !branchId.isEmpty()) return branchId;

        branchId = extractFromPathVariable(request.getRequestURI());
        if (branchId != null) return branchId;

        if (cachedBody != null && cachedBody.length > 0) {
            try {
                JsonNode jsonNode = objectMapper.readTree(cachedBody);
                JsonNode branchIdNode = jsonNode.get("branchId");
                if (branchIdNode != null && !branchIdNode.isNull()) {
                    return branchIdNode.asText();
                }
            } catch (Exception e) {
                log.debug("Could not parse request body for branchId: {}", e.getMessage());
            }
        }
        return null;
    }

    private String extractFromPathVariable(String requestURI) {
        Pattern pattern = Pattern.compile("/branches/([a-fA-F0-9-]{36})");
        Matcher matcher = pattern.matcher(requestURI);
        if (matcher.find()) return matcher.group(1);
        return null; // sessions endpoints don't encode branchId in path
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String errorResponse = String.format("{\"error\": \"%s\", \"message\": \"%s\", \"status\": %d}", status.getReasonPhrase(), message, status.value());
        response.getWriter().write(errorResponse);
    }

    // Wrapper that provides the cached body back to downstream components
    private static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] cachedBody;
        CachedBodyRequestWrapper(HttpServletRequest request, byte[] cachedBody) {
            super(request);
            this.cachedBody = (cachedBody != null) ? cachedBody : new byte[0];
        }
        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override public int read() { return byteArrayInputStream.read(); }
                @Override public boolean isFinished() { return byteArrayInputStream.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener readListener) { /* no-op */ }
            };
        }
        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}

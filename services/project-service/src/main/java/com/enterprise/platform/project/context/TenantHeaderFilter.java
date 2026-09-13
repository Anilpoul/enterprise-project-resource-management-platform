package com.enterprise.platform.project.context;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantHeaderFilter extends OncePerRequestFilter {

    public static final String HEADER_ORGANIZATION_ID = "X-Organization-Id";
    public static final String HEADER_USER_ID = "X-User-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String orgIdHeader = request.getHeader(HEADER_ORGANIZATION_ID);
        String userIdHeader = request.getHeader(HEADER_USER_ID);

        try {
            if (orgIdHeader != null && !orgIdHeader.isBlank()) {
                try {
                    TenantContext.setOrganizationId(UUID.fromString(orgIdHeader.trim()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid UUID format in X-Organization-Id header: {}", orgIdHeader);
                }
            }

            if (userIdHeader != null && !userIdHeader.isBlank()) {
                try {
                    TenantContext.setUserId(UUID.fromString(userIdHeader.trim()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid UUID format in X-User-Id header: {}", userIdHeader);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}

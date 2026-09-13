package com.enterprise.platform.analytics.context;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantHeaderFilter implements Filter {

    public static final String HEADER_ORGANIZATION_ID = "X-Organization-Id";
    public static final String HEADER_USER_ID = "X-User-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String orgIdHeader = httpRequest.getHeader(HEADER_ORGANIZATION_ID);
            String userIdHeader = httpRequest.getHeader(HEADER_USER_ID);

            try {
                if (orgIdHeader != null && !orgIdHeader.isBlank()) {
                    TenantContext.setOrganizationId(UUID.fromString(orgIdHeader.trim()));
                }
                if (userIdHeader != null && !userIdHeader.isBlank()) {
                    TenantContext.setUserId(UUID.fromString(userIdHeader.trim()));
                }
                chain.doFilter(request, response);
            } finally {
                TenantContext.clear();
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}

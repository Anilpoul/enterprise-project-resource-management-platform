package com.enterprise.platform.gateway.security;

import java.util.List;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final List<String> PUBLIC_ENDPOINTS =
            List.of(

                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/refresh",
                    "/api/auth/refresh-token",

                    "/swagger-ui",
                    "/swagger-ui/",
                    "/swagger-ui/**",

                    "/v3/api-docs",
                    "/v3/api-docs/**",

                    "/actuator",
                    "/actuator/**"
            );
}
package com.enterprise.platform.gateway.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static com.enterprise.platform.gateway.security.SecurityConstants.PUBLIC_ENDPOINTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        implements GlobalFilter, Ordered {

    private final JwtService jwtService;
    private final AuthorizationService authorizationService;

    @PostConstruct
    public void init() {
        log.info("JWT Authentication Filter Loaded");
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain
    ) {

        log.info(
                "JWT Filter Invoked. Path={}",
                exchange.getRequest()
                        .getURI()
                        .getPath()
        );

        String path =
                exchange.getRequest()
                        .getURI()
                        .getPath();
        String method =
                exchange.getRequest()
                        .getMethod()
                        .name();

        if (isPublicEndpoint(path)) {

            return chain.filter(exchange);
        }

        String authHeader =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(
                                HttpHeaders.AUTHORIZATION
                        );

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            exchange.getResponse()
                    .setStatusCode(
                            HttpStatus.UNAUTHORIZED
                    );

            return exchange.getResponse()
                    .setComplete();
        }

        String token =
                authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {

            exchange.getResponse()
                    .setStatusCode(
                            HttpStatus.UNAUTHORIZED
                    );

            return exchange.getResponse()
                    .setComplete();
        }

        String email =
                jwtService.extractUsername(token);
        String userId =
                jwtService.extractUserId(token);

        List<String> roles =
                Optional.ofNullable(
                        jwtService.extractRoles(token)
                ).orElse(List.of());

        List<String> permissions =
                Optional.ofNullable(
                        jwtService.extractPermissions(token)
                ).orElse(List.of());

        if (isAdminOnlyRoute(path, method)
                && !authorizationService.hasRole(
                roles,
                RoutePermissionRegistry.ROLE_ADMIN
        )) {

            log.warn(
                    "Access denied. User roles={} Path={}",
                    roles,
                    path
            );

            exchange.getResponse()
                    .setStatusCode(
                            HttpStatus.FORBIDDEN
                    );

            return exchange.getResponse()
                    .setComplete();
        }

        ServerHttpRequest modifiedRequest =
                exchange.getRequest()
                        .mutate()
                        .header(
                                "X-User-Id",
                                userId
                        )
                        .header(
                                "X-User-Email",
                                email
                        )
                        .header(
                                "X-User-Roles",
                                String.join(",", roles)
                        )
                        .header(
                                "X-User-Permissions",
                                String.join(",", permissions)
                        )
                        .build();

        return chain.filter(
                exchange.mutate()
                        .request(modifiedRequest)
                        .build()
        );
    }

    private boolean isPublicEndpoint(String path) {

        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(path::contains)
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator");
    }

    @Override
    public int getOrder() {

        return -1;
    }

    private boolean isAdminOnlyRoute(
            String path,
            String method
    ) {

        boolean protectedResource =
                path.contains("/departments")
                        || path.contains("/designations");

        boolean protectedMethod =
                method.equals("POST")
                        || method.equals("PUT")
                        || method.equals("PATCH")
                        || method.equals("DELETE");

        return protectedResource
                && protectedMethod;
    }

}
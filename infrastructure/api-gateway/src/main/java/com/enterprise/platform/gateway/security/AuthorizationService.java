package com.enterprise.platform.gateway.security;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorizationService {

    public boolean hasRole(
            List<String> userRoles,
            String requiredRole
    ) {

        return userRoles != null
                && userRoles.contains(requiredRole);
    }
}
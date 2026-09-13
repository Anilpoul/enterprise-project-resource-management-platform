package com.enterprise.platform.auth.security.jwt;

import com.enterprise.platform.auth.entity.Permission;
import com.enterprise.platform.auth.entity.Role;
import com.enterprise.platform.auth.entity.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JwtClaimsFactory {

    public Map<String, Object> buildClaims(
            User user
    ) {

        Map<String, Object> claims =
                new HashMap<>();

        claims.put(
                "userId",
                user.getId().toString()
        );

        claims.put(
                "roles",
                user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .map(Enum::name)
                        .toList()
        );

        claims.put(
                "permissions",
                user.getRoles()
                        .stream()
                        .flatMap(role ->
                                role.getPermissions().stream()
                        )
                        .map(Permission::getName)
                        .map(Enum::name)
                        .distinct()
                        .toList()
        );

        return claims;
    }
}

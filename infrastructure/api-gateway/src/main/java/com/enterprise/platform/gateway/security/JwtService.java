package com.enterprise.platform.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    public String extractUsername(
            String token
    ) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> resolver
    ) {

        return resolver.apply(
                extractAllClaims(token)
        );
    }

    public boolean isTokenValid(
            String token
    ) {

        try {

            Claims claims =
                    extractAllClaims(token);

            return claims.getExpiration()
                    .after(new Date());

        } catch (Exception ex) {

            return false;
        }
    }

    private Claims extractAllClaims(
            String token
    ) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {

        byte[] keyBytes =
                Decoders.BASE64.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserId(
            String token
    ) {

        return extractClaim(
                token,
                claims -> claims.get(
                        "userId",
                        String.class
                )
        );
    }

    public List<String> extractRoles(
            String token
    ) {

        return extractClaim(
                token,
                claims -> claims.get(
                        "roles",
                        List.class
                )
        );
    }

    public List<String> extractPermissions(
            String token
    ) {

        return extractClaim(
                token,
                claims -> claims.get(
                        "permissions",
                        List.class
                )
        );
    }
}
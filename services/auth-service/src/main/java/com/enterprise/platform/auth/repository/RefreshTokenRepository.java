package com.enterprise.platform.auth.repository;

import com.enterprise.platform.auth.entity.RefreshToken;
import com.enterprise.platform.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);
    List<RefreshToken> findAllByUser(User user);
    @Modifying
    @Query("""
DELETE FROM RefreshToken r
WHERE r.expiryDate < :now
""")
    int deleteExpiredTokens(LocalDateTime now);

    @Modifying
    @Query("""
UPDATE RefreshToken r
SET r.revoked = true
WHERE r.user.id = :userId
""")
    void revokeAllUserTokens(
            UUID userId
    );

}
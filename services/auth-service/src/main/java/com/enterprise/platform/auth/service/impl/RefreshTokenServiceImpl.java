package com.enterprise.platform.auth.service.impl;

import com.enterprise.platform.auth.entity.RefreshToken;
import com.enterprise.platform.auth.entity.User;
import com.enterprise.platform.auth.exception.BadRequestException;
import com.enterprise.platform.auth.repository.RefreshTokenRepository;
import com.enterprise.platform.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);

        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken.setExpiryDate(
                LocalDateTime.now().plusDays(7)
        );

        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Invalid refresh token"
                                )
                        );

        if (refreshToken.getRevoked()) {

            throw new BadRequestException(
                    "Refresh token revoked"
            );
        }

        if (refreshToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "Refresh token expired"
            );
        }

        return refreshToken;
    }

    @Override
    public void revokeToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Invalid refresh token"
                                )
                        );

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public RefreshToken rotateRefreshToken(
            RefreshToken oldToken
    ) {

        oldToken.setRevoked(true);

        refreshTokenRepository.save(oldToken);

        RefreshToken newToken = new RefreshToken();

        newToken.setUser(oldToken.getUser());

        newToken.setToken(UUID.randomUUID().toString());

        newToken.setExpiryDate(
                LocalDateTime.now().plusDays(7)
        );

        newToken.setRevoked(false);

        return refreshTokenRepository.save(newToken);
    }

    @Transactional
    @Override
    public void cleanupExpiredTokens() {

        int deletedCount =
                refreshTokenRepository.deleteExpiredTokens(
                        LocalDateTime.now()
                );

        log.info(
                "Deleted {} expired refresh tokens",
                deletedCount
        );
    }

}
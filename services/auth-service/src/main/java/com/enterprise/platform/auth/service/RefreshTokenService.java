package com.enterprise.platform.auth.service;

import com.enterprise.platform.auth.entity.RefreshToken;
import com.enterprise.platform.auth.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);

    void revokeToken(String token);

    RefreshToken rotateRefreshToken(RefreshToken oldToken);

    void cleanupExpiredTokens();

}
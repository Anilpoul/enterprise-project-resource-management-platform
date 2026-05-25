package com.enterprise.platform.auth.service.token;

public interface TokenBlacklistService {

    void blacklistToken(String token);

    boolean isBlacklisted(String token);

}
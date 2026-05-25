package com.enterprise.platform.auth.service.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl
        implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX =
            "blacklisted_token:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void blacklistToken(String token) {

        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "blacklisted",
                Duration.ofDays(1)
        );
    }

    @Override
    public boolean isBlacklisted(String token) {

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(
                        BLACKLIST_PREFIX + token
                )
        );
    }

}
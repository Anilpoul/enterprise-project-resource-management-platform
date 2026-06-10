package com.enterprise.platform.auth.schedular;

import com.enterprise.platform.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenService
            refreshTokenService;

    @Scheduled(
            cron = "0 1/10 * * * ?"
    )
    public void cleanup() {

        refreshTokenService
                .cleanupExpiredTokens();

        log.info(
                "Expired refresh tokens cleaned"
        );
    }
}

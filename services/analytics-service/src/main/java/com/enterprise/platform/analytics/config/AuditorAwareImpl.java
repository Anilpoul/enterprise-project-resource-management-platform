package com.enterprise.platform.analytics.config;

import com.enterprise.platform.analytics.context.TenantContext;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;
import java.util.UUID;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        UUID userId = TenantContext.getUserId();
        return Optional.ofNullable(userId != null ? userId.toString() : "SYSTEM");
    }
}

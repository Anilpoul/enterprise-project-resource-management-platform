package com.enterprise.platform.organization.config;

import com.enterprise.platform.organization.context.TenantContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        UUID userId = TenantContext.getUserId();
        if (userId != null) {
            return Optional.of(userId.toString());
        }
        return Optional.of("SYSTEM");
    }
}

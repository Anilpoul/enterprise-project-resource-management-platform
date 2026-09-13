package com.enterprise.platform.user.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl
        implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        /*
         * Later:
         * Extract email from JWT
         */

        return Optional.of(
                "SYSTEM"
        );
    }
}
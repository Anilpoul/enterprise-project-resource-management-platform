package com.enterprise.platform.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(
        auditorAwareRef = "auditorAwareImpl"
)
public class JpaAuditConfig {
}
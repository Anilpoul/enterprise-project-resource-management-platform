package com.enterprise.platform.audit.dto.response;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditSummaryResponse {

    private UUID organizationId;

    private long totalAuditLogs;

    private Map<String, Long> countByAction;

    private Map<String, Long> countByEntityType;

    private Map<String, Long> countByStatus;
}

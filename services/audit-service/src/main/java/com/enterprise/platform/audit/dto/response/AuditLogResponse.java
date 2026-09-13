package com.enterprise.platform.audit.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private UUID id;

    private UUID organizationId;

    private String entityType;

    private String entityId;

    private String action;

    private String performedBy;

    private String ipAddress;

    private String status;

    private String details;

    private LocalDateTime timestamp;

    private LocalDateTime createdAt;
}

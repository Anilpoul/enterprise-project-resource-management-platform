package com.enterprise.platform.audit.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSearchCriteria {

    private String entityType;

    private String entityId;

    private String action;

    private String performedBy;

    private String status;

    private LocalDateTime fromTimestamp;

    private LocalDateTime toTimestamp;

    private String keyword;
}

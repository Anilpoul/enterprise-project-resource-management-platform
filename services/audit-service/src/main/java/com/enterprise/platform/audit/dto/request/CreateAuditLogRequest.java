package com.enterprise.platform.audit.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuditLogRequest {

    private UUID organizationId;

    @NotBlank(message = "Entity type is required")
    private String entityType;

    private String entityId;

    @NotBlank(message = "Action is required")
    private String action;

    private String performedBy;

    private String ipAddress;

    @Builder.Default
    private String status = "SUCCESS";

    private String details;

    private LocalDateTime timestamp;
}

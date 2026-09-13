package com.enterprise.platform.events;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectEvent {

    private UUID eventId;

    private ProjectEventType eventType;

    private UUID organizationId;

    private UUID projectId;

    private String projectName;

    private String projectKey;

    private UUID userId;

    private String memberRole;

    private LocalDateTime timestamp;

    private String details;
}

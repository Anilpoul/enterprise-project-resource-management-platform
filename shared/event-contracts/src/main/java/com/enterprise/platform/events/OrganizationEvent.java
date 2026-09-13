package com.enterprise.platform.events;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationEvent {

    private UUID eventId;

    private OrganizationEventType eventType;

    private UUID organizationId;

    private String organizationName;

    private String organizationSlug;

    private UUID userId;

    private String memberRole;

    private LocalDateTime timestamp;

    private String details;
}

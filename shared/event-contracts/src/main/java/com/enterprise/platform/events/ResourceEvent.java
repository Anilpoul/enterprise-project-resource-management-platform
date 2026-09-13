package com.enterprise.platform.events;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceEvent {

    private UUID eventId;

    private ResourceEventType eventType;

    private UUID organizationId;

    private UUID resourceId;

    private UUID userId;

    private UUID projectId;

    private UUID allocationId;

    private Integer allocationPercentage;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime timestamp;

    private String details;
}

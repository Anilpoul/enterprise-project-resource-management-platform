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
public class SprintEvent {

    private UUID eventId;

    private SprintEventType eventType;

    private UUID organizationId;

    private UUID projectId;

    private UUID sprintId;

    private String sprintName;

    private String status;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime timestamp;

    private String details;
}

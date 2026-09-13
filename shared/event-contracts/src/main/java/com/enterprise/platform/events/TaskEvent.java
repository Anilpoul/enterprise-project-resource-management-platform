package com.enterprise.platform.events;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEvent {

    private UUID eventId;

    private TaskEventType eventType;

    private UUID organizationId;

    private UUID projectId;

    private UUID taskId;

    private String taskKey;

    private String title;

    private String taskType;

    private String status;

    private String priority;

    private UUID assigneeId;

    private UUID reporterId;

    private LocalDateTime timestamp;

    private String details;
}

package com.enterprise.platform.task.dto.response;

import com.enterprise.platform.task.constants.enums.TaskPriority;
import com.enterprise.platform.task.constants.enums.TaskStatus;
import com.enterprise.platform.task.constants.enums.TaskType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private UUID id;

    private UUID organizationId;

    private UUID projectId;

    private String taskKey;

    private String title;

    private String description;

    private TaskType taskType;

    private TaskStatus status;

    private TaskPriority priority;

    private UUID assigneeId;

    private UUID reporterId;

    private UUID parentTaskId;

    private Integer storyPoints;

    private BigDecimal estimatedHours;

    private BigDecimal loggedHours;

    private LocalDate dueDate;

    private UUID sprintId;

    private UUID milestoneId;

    private int commentCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

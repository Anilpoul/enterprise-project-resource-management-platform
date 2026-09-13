package com.enterprise.platform.task.dto.request;

import com.enterprise.platform.task.constants.enums.TaskPriority;
import com.enterprise.platform.task.constants.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskRequest {

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotBlank(message = "Project key is required")
    private String projectKey;

    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 255, message = "Title must be between 2 and 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Task type is required")
    private TaskType taskType;

    private TaskPriority priority;

    private UUID assigneeId;

    private UUID reporterId;

    private UUID parentTaskId;

    private Integer storyPoints;

    private BigDecimal estimatedHours;

    private LocalDate dueDate;

    private UUID sprintId;

    private UUID milestoneId;
}

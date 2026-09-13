package com.enterprise.platform.task.dto.request;

import com.enterprise.platform.task.constants.enums.TaskPriority;
import com.enterprise.platform.task.constants.enums.TaskType;
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
public class UpdateTaskRequest {

    @Size(min = 2, max = 255, message = "Title must be between 2 and 255 characters")
    private String title;

    private String description;

    private TaskType taskType;

    private TaskPriority priority;

    private UUID assigneeId;

    private Integer storyPoints;

    private BigDecimal estimatedHours;

    private BigDecimal loggedHours;

    private LocalDate dueDate;

    private UUID sprintId;

    private UUID milestoneId;
}

package com.enterprise.platform.task.dto.request;

import com.enterprise.platform.task.constants.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStatusUpdateRequest {

    @NotNull(message = "Task status is required")
    private TaskStatus status;
}

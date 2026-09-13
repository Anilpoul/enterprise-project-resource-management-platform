package com.enterprise.platform.task.dto.request;

import com.enterprise.platform.task.constants.enums.TaskPriority;
import com.enterprise.platform.task.constants.enums.TaskStatus;
import com.enterprise.platform.task.constants.enums.TaskType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSearchCriteria {

    private UUID projectId;

    private String search;

    private TaskStatus status;

    private TaskType taskType;

    private TaskPriority priority;

    private UUID assigneeId;

    private UUID reporterId;

    private UUID sprintId;

    private UUID parentTaskId;
}

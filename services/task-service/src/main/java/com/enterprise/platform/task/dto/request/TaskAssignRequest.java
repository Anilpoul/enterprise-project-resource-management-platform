package com.enterprise.platform.task.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignRequest {

    @NotNull(message = "Assignee user ID is required")
    private UUID assigneeId;
}

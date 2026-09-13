package com.enterprise.platform.task.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCommentResponse {

    private UUID id;

    private UUID taskId;

    private UUID organizationId;

    private UUID userId;

    private String comment;

    private LocalDateTime createdAt;
}

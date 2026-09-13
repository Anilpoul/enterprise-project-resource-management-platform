package com.enterprise.platform.project.dto.response;

import com.enterprise.platform.project.constants.enums.MilestoneStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMilestoneResponse {

    private UUID id;

    private UUID projectId;

    private UUID organizationId;

    private String name;

    private String description;

    private LocalDate dueDate;

    private MilestoneStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

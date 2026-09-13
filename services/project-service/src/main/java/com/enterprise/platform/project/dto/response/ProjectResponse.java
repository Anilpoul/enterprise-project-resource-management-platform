package com.enterprise.platform.project.dto.response;

import com.enterprise.platform.project.constants.enums.ProjectStatus;
import com.enterprise.platform.project.constants.enums.ProjectType;
import com.enterprise.platform.project.constants.enums.ProjectVisibility;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private UUID id;

    private UUID organizationId;

    private String name;

    private String projectKey;

    private String description;

    private ProjectType projectType;

    private ProjectStatus status;

    private ProjectVisibility visibility;

    private UUID leadUserId;

    private LocalDate startDate;

    private LocalDate targetEndDate;

    private LocalDate actualEndDate;

    private BigDecimal budget;

    private String currency;

    private long memberCount;

    private long milestoneCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

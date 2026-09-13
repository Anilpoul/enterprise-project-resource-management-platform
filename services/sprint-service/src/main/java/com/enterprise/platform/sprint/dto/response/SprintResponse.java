package com.enterprise.platform.sprint.dto.response;

import com.enterprise.platform.sprint.constants.enums.SprintStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintResponse {

    private UUID id;

    private UUID organizationId;

    private UUID projectId;

    private String name;

    private String goal;

    private SprintStatus status;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime completedAt;

    private Integer totalStoryPoints;

    private Integer completedStoryPoints;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

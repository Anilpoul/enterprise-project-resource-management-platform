package com.enterprise.platform.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintAnalyticsResponse {

    private UUID id;
    private UUID sprintId;
    private UUID projectId;
    private String sprintName;
    private Integer committedStoryPoints;
    private Integer completedStoryPoints;
    private BigDecimal velocity;
    private BigDecimal completionRate;
    private Integer spilloverTasksCount;
    private String status;
    private LocalDateTime updatedAt;
}

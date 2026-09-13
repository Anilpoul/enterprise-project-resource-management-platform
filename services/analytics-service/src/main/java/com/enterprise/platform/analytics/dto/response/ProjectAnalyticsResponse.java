package com.enterprise.platform.analytics.dto.response;

import com.enterprise.platform.analytics.enums.ProjectHealthStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAnalyticsResponse {

    private UUID id;
    private UUID projectId;
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer inProgressTasks;
    private Integer blockedTasks;
    private Integer overdueTasks;
    private Integer totalStoryPoints;
    private Integer completedStoryPoints;
    private BigDecimal completionRate;
    private ProjectHealthStatus healthStatus;
    private LocalDateTime lastCalculatedAt;
}

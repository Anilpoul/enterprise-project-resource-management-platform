package com.enterprise.platform.analytics.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDashboardResponse {

    private UUID organizationId;
    private long totalProjects;
    private long totalTasks;
    private long completedTasks;
    private BigDecimal overallCompletionRate;
    private BigDecimal averageSprintVelocity;
    private List<EmployeeScorecardResponse> topPerformers;
    private List<ProjectAnalyticsResponse> projectSummaries;
}

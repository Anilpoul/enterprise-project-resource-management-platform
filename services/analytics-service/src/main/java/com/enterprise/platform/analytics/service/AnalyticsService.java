package com.enterprise.platform.analytics.service;

import com.enterprise.platform.analytics.dto.response.OrganizationDashboardResponse;
import com.enterprise.platform.analytics.dto.response.ProjectAnalyticsResponse;
import com.enterprise.platform.analytics.dto.response.SprintAnalyticsResponse;

import java.util.List;
import java.util.UUID;

public interface AnalyticsService {

    OrganizationDashboardResponse getOrganizationDashboard();

    ProjectAnalyticsResponse getProjectAnalytics(UUID projectId);

    SprintAnalyticsResponse getSprintAnalytics(UUID sprintId);

    List<SprintAnalyticsResponse> getProjectSprintsAnalytics(UUID projectId);

    List<ProjectAnalyticsResponse> getAllProjectsAnalytics();
}

package com.enterprise.platform.analytics.controller;

import com.enterprise.platform.analytics.dto.response.ApiResponse;
import com.enterprise.platform.analytics.dto.response.ProjectAnalyticsResponse;
import com.enterprise.platform.analytics.dto.response.SprintAnalyticsResponse;
import com.enterprise.platform.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics/projects")
@RequiredArgsConstructor
@Tag(name = "Project Analytics", description = "Project health, task completion rates, and delivery trends")
public class ProjectAnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{projectId}")
    @Operation(summary = "Get project analytics snapshot")
    public ResponseEntity<ApiResponse<ProjectAnalyticsResponse>> getProjectAnalytics(@PathVariable UUID projectId) {
        log.info("REST request to get project analytics for project: {}", projectId);
        ProjectAnalyticsResponse response = analyticsService.getProjectAnalytics(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{projectId}/sprints")
    @Operation(summary = "Get sprint analytics history for project")
    public ResponseEntity<ApiResponse<List<SprintAnalyticsResponse>>> getProjectSprintsAnalytics(@PathVariable UUID projectId) {
        log.info("REST request to get sprints analytics for project: {}", projectId);
        List<SprintAnalyticsResponse> response = analyticsService.getProjectSprintsAnalytics(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

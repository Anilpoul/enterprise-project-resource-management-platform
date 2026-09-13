package com.enterprise.platform.analytics.controller;

import com.enterprise.platform.analytics.dto.response.ApiResponse;
import com.enterprise.platform.analytics.dto.response.OrganizationDashboardResponse;
import com.enterprise.platform.analytics.dto.response.ProjectAnalyticsResponse;
import com.enterprise.platform.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics Dashboard", description = "Executive KPI Dashboard and high-level enterprise metrics")
public class AnalyticsDashboardController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get organization executive analytics dashboard")
    public ResponseEntity<ApiResponse<OrganizationDashboardResponse>> getDashboard() {
        log.info("REST request to get organization analytics dashboard");
        OrganizationDashboardResponse response = analyticsService.getOrganizationDashboard();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/projects")
    @Operation(summary = "Get analytics for all organization projects")
    public ResponseEntity<ApiResponse<List<ProjectAnalyticsResponse>>> getAllProjectsAnalytics() {
        log.info("REST request to get all projects analytics");
        List<ProjectAnalyticsResponse> response = analyticsService.getAllProjectsAnalytics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

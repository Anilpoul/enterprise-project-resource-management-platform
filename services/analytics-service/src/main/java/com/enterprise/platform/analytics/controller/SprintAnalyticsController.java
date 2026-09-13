package com.enterprise.platform.analytics.controller;

import com.enterprise.platform.analytics.dto.response.ApiResponse;
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

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprint Analytics", description = "Sprint velocity, committed vs completed story points, and burndown metrics")
public class SprintAnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{sprintId}")
    @Operation(summary = "Get sprint analytics snapshot")
    public ResponseEntity<ApiResponse<SprintAnalyticsResponse>> getSprintAnalytics(@PathVariable UUID sprintId) {
        log.info("REST request to get sprint analytics for sprint: {}", sprintId);
        SprintAnalyticsResponse response = analyticsService.getSprintAnalytics(sprintId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

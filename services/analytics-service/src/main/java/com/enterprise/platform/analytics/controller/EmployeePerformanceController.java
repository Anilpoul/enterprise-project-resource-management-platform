package com.enterprise.platform.analytics.controller;

import com.enterprise.platform.analytics.dto.response.ApiResponse;
import com.enterprise.platform.analytics.dto.response.EmployeeScorecardResponse;
import com.enterprise.platform.analytics.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Performance Analytics", description = "Performance scorecards, story point delivery, and productivity leaderboards")
public class EmployeePerformanceController {

    private final PerformanceService performanceService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get employee productivity scorecard")
    public ResponseEntity<ApiResponse<EmployeeScorecardResponse>> getEmployeeScorecard(@PathVariable UUID userId) {
        log.info("REST request to get employee scorecard for user: {}", userId);
        EmployeeScorecardResponse response = performanceService.getEmployeeScorecard(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Get top performing employees leaderboard")
    public ResponseEntity<ApiResponse<List<EmployeeScorecardResponse>>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("REST request to get employee leaderboard (limit={})", limit);
        List<EmployeeScorecardResponse> response = performanceService.getLeaderboard(limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all employee performance scorecards")
    public ResponseEntity<ApiResponse<List<EmployeeScorecardResponse>>> getAllEmployeeScorecards() {
        log.info("REST request to get all employee scorecards");
        List<EmployeeScorecardResponse> response = performanceService.getAllEmployeeScorecards();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

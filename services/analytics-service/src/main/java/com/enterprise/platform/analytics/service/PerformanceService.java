package com.enterprise.platform.analytics.service;

import com.enterprise.platform.analytics.dto.response.EmployeeScorecardResponse;

import java.util.List;
import java.util.UUID;

public interface PerformanceService {

    EmployeeScorecardResponse getEmployeeScorecard(UUID userId);

    List<EmployeeScorecardResponse> getLeaderboard(int limit);

    List<EmployeeScorecardResponse> getAllEmployeeScorecards();
}

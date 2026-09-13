package com.enterprise.platform.analytics.service.impl;

import com.enterprise.platform.analytics.context.TenantContext;
import com.enterprise.platform.analytics.dto.response.EmployeeScorecardResponse;
import com.enterprise.platform.analytics.entity.EmployeePerformanceMetric;
import com.enterprise.platform.analytics.exception.BadRequestException;
import com.enterprise.platform.analytics.exception.ResourceNotFoundException;
import com.enterprise.platform.analytics.mapper.AnalyticsMapper;
import com.enterprise.platform.analytics.repository.EmployeePerformanceRepository;
import com.enterprise.platform.analytics.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private final EmployeePerformanceRepository employeePerformanceRepository;
    private final AnalyticsMapper analyticsMapper;

    private UUID getRequiredOrgId() {
        UUID orgId = TenantContext.getOrganizationId();
        if (orgId == null) {
            throw new BadRequestException("Missing X-Organization-Id header for tenant isolation");
        }
        return orgId;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeScorecardResponse getEmployeeScorecard(UUID userId) {
        UUID orgId = getRequiredOrgId();
        log.info("Fetching scorecard for employee: {} in org: {}", userId, orgId);

        EmployeePerformanceMetric metric = employeePerformanceRepository.findByOrganizationIdAndUserId(orgId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee performance scorecard not found for user ID: " + userId));

        return analyticsMapper.toResponse(metric);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeScorecardResponse> getLeaderboard(int limit) {
        UUID orgId = getRequiredOrgId();
        log.info("Fetching employee leaderboard (limit={}) for org: {}", limit, orgId);

        List<EmployeePerformanceMetric> list = employeePerformanceRepository.findByOrganizationIdOrderByPerformanceScoreDesc(orgId);
        return list.stream()
                .limit(limit > 0 ? limit : 10)
                .map(analyticsMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeScorecardResponse> getAllEmployeeScorecards() {
        UUID orgId = getRequiredOrgId();
        log.info("Fetching all employee scorecards for org: {}", orgId);

        List<EmployeePerformanceMetric> list = employeePerformanceRepository.findByOrganizationId(orgId);
        return list.stream()
                .map(analyticsMapper::toResponse)
                .toList();
    }
}

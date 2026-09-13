package com.enterprise.platform.analytics.service.impl;

import com.enterprise.platform.analytics.context.TenantContext;
import com.enterprise.platform.analytics.dto.response.EmployeeScorecardResponse;
import com.enterprise.platform.analytics.dto.response.OrganizationDashboardResponse;
import com.enterprise.platform.analytics.dto.response.ProjectAnalyticsResponse;
import com.enterprise.platform.analytics.dto.response.SprintAnalyticsResponse;
import com.enterprise.platform.analytics.entity.ProjectMetricSnapshot;
import com.enterprise.platform.analytics.entity.SprintMetricSnapshot;
import com.enterprise.platform.analytics.exception.BadRequestException;
import com.enterprise.platform.analytics.exception.ResourceNotFoundException;
import com.enterprise.platform.analytics.mapper.AnalyticsMapper;
import com.enterprise.platform.analytics.repository.ProjectMetricRepository;
import com.enterprise.platform.analytics.repository.SprintMetricRepository;
import com.enterprise.platform.analytics.service.AnalyticsService;
import com.enterprise.platform.analytics.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ProjectMetricRepository projectMetricRepository;
    private final SprintMetricRepository sprintMetricRepository;
    private final PerformanceService performanceService;
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
    public OrganizationDashboardResponse getOrganizationDashboard() {
        UUID orgId = getRequiredOrgId();
        log.info("Generating organization analytics dashboard for org: {}", orgId);

        long totalProjects = projectMetricRepository.countByOrganizationId(orgId);
        Long totalTasks = projectMetricRepository.sumTotalTasksByOrganizationId(orgId);
        Long completedTasks = projectMetricRepository.sumCompletedTasksByOrganizationId(orgId);

        long totalTasksVal = totalTasks != null ? totalTasks : 0L;
        long completedTasksVal = completedTasks != null ? completedTasks : 0L;

        BigDecimal overallRate = totalTasksVal > 0
                ? new BigDecimal(completedTasksVal).multiply(new BigDecimal("100.00")).divide(new BigDecimal(totalTasksVal), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<SprintMetricSnapshot> sprints = sprintMetricRepository.findByOrganizationId(orgId);
        BigDecimal avgVelocity = BigDecimal.ZERO;
        if (!sprints.isEmpty()) {
            BigDecimal totalVelocity = sprints.stream()
                    .map(SprintMetricSnapshot::getVelocity)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgVelocity = totalVelocity.divide(new BigDecimal(sprints.size()), 2, RoundingMode.HALF_UP);
        }

        List<EmployeeScorecardResponse> topPerformers = performanceService.getLeaderboard(5);

        List<ProjectMetricSnapshot> projectSnapshots = projectMetricRepository.findByOrganizationId(orgId);
        List<ProjectAnalyticsResponse> projectSummaries = projectSnapshots.stream()
                .map(analyticsMapper::toResponse)
                .toList();

        return OrganizationDashboardResponse.builder()
                .organizationId(orgId)
                .totalProjects(totalProjects)
                .totalTasks(totalTasksVal)
                .completedTasks(completedTasksVal)
                .overallCompletionRate(overallRate)
                .averageSprintVelocity(avgVelocity)
                .topPerformers(topPerformers)
                .projectSummaries(projectSummaries)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectAnalyticsResponse getProjectAnalytics(UUID projectId) {
        UUID orgId = getRequiredOrgId();
        log.info("Fetching project analytics for project: {} in org: {}", projectId, orgId);

        ProjectMetricSnapshot snapshot = projectMetricRepository.findByOrganizationIdAndProjectId(orgId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project analytics not found for project ID: " + projectId));

        return analyticsMapper.toResponse(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public SprintAnalyticsResponse getSprintAnalytics(UUID sprintId) {
        UUID orgId = getRequiredOrgId();
        log.info("Fetching sprint analytics for sprint: {} in org: {}", sprintId, orgId);

        SprintMetricSnapshot snapshot = sprintMetricRepository.findByOrganizationIdAndSprintId(orgId, sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint analytics not found for sprint ID: " + sprintId));

        return analyticsMapper.toResponse(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprintAnalyticsResponse> getProjectSprintsAnalytics(UUID projectId) {
        UUID orgId = getRequiredOrgId();
        log.info("Fetching sprints analytics for project: {} in org: {}", projectId, orgId);

        List<SprintMetricSnapshot> snapshots = sprintMetricRepository.findByOrganizationIdAndProjectId(orgId, projectId);
        return snapshots.stream()
                .map(analyticsMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectAnalyticsResponse> getAllProjectsAnalytics() {
        UUID orgId = getRequiredOrgId();
        log.info("Fetching all projects analytics for org: {}", orgId);

        List<ProjectMetricSnapshot> snapshots = projectMetricRepository.findByOrganizationId(orgId);
        return snapshots.stream()
                .map(analyticsMapper::toResponse)
                .toList();
    }
}

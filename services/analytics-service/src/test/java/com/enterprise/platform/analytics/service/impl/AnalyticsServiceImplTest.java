package com.enterprise.platform.analytics.service.impl;

import com.enterprise.platform.analytics.context.TenantContext;
import com.enterprise.platform.analytics.dto.response.EmployeeScorecardResponse;
import com.enterprise.platform.analytics.dto.response.OrganizationDashboardResponse;
import com.enterprise.platform.analytics.dto.response.ProjectAnalyticsResponse;
import com.enterprise.platform.analytics.dto.response.SprintAnalyticsResponse;
import com.enterprise.platform.analytics.entity.ProjectMetricSnapshot;
import com.enterprise.platform.analytics.entity.SprintMetricSnapshot;
import com.enterprise.platform.analytics.enums.PerformanceRating;
import com.enterprise.platform.analytics.enums.ProjectHealthStatus;
import com.enterprise.platform.analytics.exception.BadRequestException;
import com.enterprise.platform.analytics.exception.ResourceNotFoundException;
import com.enterprise.platform.analytics.mapper.AnalyticsMapper;
import com.enterprise.platform.analytics.repository.ProjectMetricRepository;
import com.enterprise.platform.analytics.repository.SprintMetricRepository;
import com.enterprise.platform.analytics.service.PerformanceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private ProjectMetricRepository projectMetricRepository;

    @Mock
    private SprintMetricRepository sprintMetricRepository;

    @Mock
    private PerformanceService performanceService;

    @Mock
    private AnalyticsMapper analyticsMapper;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private UUID organizationId;
    private UUID projectId;
    private UUID sprintId;
    private ProjectMetricSnapshot projectSnapshot;
    private ProjectAnalyticsResponse projectResponse;
    private SprintMetricSnapshot sprintSnapshot;
    private SprintAnalyticsResponse sprintResponse;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        sprintId = UUID.randomUUID();

        TenantContext.setOrganizationId(organizationId);

        projectSnapshot = ProjectMetricSnapshot.builder()
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .projectId(projectId)
                .totalTasks(20)
                .completedTasks(15)
                .inProgressTasks(3)
                .blockedTasks(2)
                .completionRate(new BigDecimal("75.00"))
                .healthStatus(ProjectHealthStatus.ON_TRACK)
                .build();

        projectResponse = ProjectAnalyticsResponse.builder()
                .projectId(projectId)
                .totalTasks(20)
                .completedTasks(15)
                .completionRate(new BigDecimal("75.00"))
                .healthStatus(ProjectHealthStatus.ON_TRACK)
                .build();

        sprintSnapshot = SprintMetricSnapshot.builder()
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .projectId(projectId)
                .sprintId(sprintId)
                .sprintName("Sprint 1")
                .committedStoryPoints(30)
                .completedStoryPoints(28)
                .velocity(new BigDecimal("28.00"))
                .completionRate(new BigDecimal("93.33"))
                .status("COMPLETED")
                .build();

        sprintResponse = SprintAnalyticsResponse.builder()
                .sprintId(sprintId)
                .projectId(projectId)
                .sprintName("Sprint 1")
                .velocity(new BigDecimal("28.00"))
                .completionRate(new BigDecimal("93.33"))
                .status("COMPLETED")
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should get organization dashboard successfully")
    void testGetOrganizationDashboard_Success() {
        when(projectMetricRepository.countByOrganizationId(organizationId)).thenReturn(2L);
        when(projectMetricRepository.sumTotalTasksByOrganizationId(organizationId)).thenReturn(40L);
        when(projectMetricRepository.sumCompletedTasksByOrganizationId(organizationId)).thenReturn(30L);
        when(sprintMetricRepository.findByOrganizationId(organizationId)).thenReturn(List.of(sprintSnapshot));

        EmployeeScorecardResponse performer = EmployeeScorecardResponse.builder()
                .userId(UUID.randomUUID())
                .performanceScore(new BigDecimal("95.00"))
                .performanceRating(PerformanceRating.EXCELLENT)
                .build();
        when(performanceService.getLeaderboard(5)).thenReturn(List.of(performer));
        when(projectMetricRepository.findByOrganizationId(organizationId)).thenReturn(List.of(projectSnapshot));
        when(analyticsMapper.toResponse(projectSnapshot)).thenReturn(projectResponse);

        OrganizationDashboardResponse dashboard = analyticsService.getOrganizationDashboard();

        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getTotalProjects()).isEqualTo(2);
        assertThat(dashboard.getTotalTasks()).isEqualTo(40);
        assertThat(dashboard.getCompletedTasks()).isEqualTo(30);
        assertThat(dashboard.getOverallCompletionRate()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(dashboard.getAverageSprintVelocity()).isEqualByComparingTo(new BigDecimal("28.00"));
        assertThat(dashboard.getTopPerformers()).hasSize(1);
        assertThat(dashboard.getProjectSummaries()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw BadRequestException when TenantContext is missing")
    void testGetOrganizationDashboard_MissingTenant_ThrowsBadRequest() {
        TenantContext.clear();

        assertThatThrownBy(() -> analyticsService.getOrganizationDashboard())
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Missing X-Organization-Id header");
    }

    @Test
    @DisplayName("Should get project analytics successfully")
    void testGetProjectAnalytics_Success() {
        when(projectMetricRepository.findByOrganizationIdAndProjectId(organizationId, projectId))
                .thenReturn(Optional.of(projectSnapshot));
        when(analyticsMapper.toResponse(projectSnapshot)).thenReturn(projectResponse);

        ProjectAnalyticsResponse result = analyticsService.getProjectAnalytics(projectId);

        assertThat(result).isNotNull();
        assertThat(result.getProjectId()).isEqualTo(projectId);
        assertThat(result.getCompletionRate()).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project analytics not found")
    void testGetProjectAnalytics_NotFound_ThrowsNotFound() {
        when(projectMetricRepository.findByOrganizationIdAndProjectId(organizationId, projectId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> analyticsService.getProjectAnalytics(projectId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project analytics not found for project ID");
    }

    @Test
    @DisplayName("Should get sprint analytics successfully")
    void testGetSprintAnalytics_Success() {
        when(sprintMetricRepository.findByOrganizationIdAndSprintId(organizationId, sprintId))
                .thenReturn(Optional.of(sprintSnapshot));
        when(analyticsMapper.toResponse(sprintSnapshot)).thenReturn(sprintResponse);

        SprintAnalyticsResponse result = analyticsService.getSprintAnalytics(sprintId);

        assertThat(result).isNotNull();
        assertThat(result.getSprintId()).isEqualTo(sprintId);
        assertThat(result.getVelocity()).isEqualByComparingTo(new BigDecimal("28.00"));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when sprint analytics not found")
    void testGetSprintAnalytics_NotFound_ThrowsNotFound() {
        when(sprintMetricRepository.findByOrganizationIdAndSprintId(organizationId, sprintId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> analyticsService.getSprintAnalytics(sprintId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sprint analytics not found for sprint ID");
    }

    @Test
    @DisplayName("Should get project sprints analytics history")
    void testGetProjectSprintsAnalytics_Success() {
        when(sprintMetricRepository.findByOrganizationIdAndProjectId(organizationId, projectId))
                .thenReturn(List.of(sprintSnapshot));
        when(analyticsMapper.toResponse(sprintSnapshot)).thenReturn(sprintResponse);

        List<SprintAnalyticsResponse> result = analyticsService.getProjectSprintsAnalytics(projectId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should get all projects analytics")
    void testGetAllProjectsAnalytics_Success() {
        when(projectMetricRepository.findByOrganizationId(organizationId))
                .thenReturn(List.of(projectSnapshot));
        when(analyticsMapper.toResponse(projectSnapshot)).thenReturn(projectResponse);

        List<ProjectAnalyticsResponse> result = analyticsService.getAllProjectsAnalytics();

        assertThat(result).hasSize(1);
    }
}

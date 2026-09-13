package com.enterprise.platform.analytics.service.impl;

import com.enterprise.platform.analytics.context.TenantContext;
import com.enterprise.platform.analytics.dto.response.EmployeeScorecardResponse;
import com.enterprise.platform.analytics.entity.EmployeePerformanceMetric;
import com.enterprise.platform.analytics.enums.PerformanceRating;
import com.enterprise.platform.analytics.exception.BadRequestException;
import com.enterprise.platform.analytics.exception.ResourceNotFoundException;
import com.enterprise.platform.analytics.mapper.AnalyticsMapper;
import com.enterprise.platform.analytics.repository.EmployeePerformanceRepository;
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
class PerformanceServiceImplTest {

    @Mock
    private EmployeePerformanceRepository employeePerformanceRepository;

    @Mock
    private AnalyticsMapper analyticsMapper;

    @InjectMocks
    private PerformanceServiceImpl performanceService;

    private UUID organizationId;
    private UUID userId;
    private EmployeePerformanceMetric metric;
    private EmployeeScorecardResponse scorecardResponse;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        userId = UUID.randomUUID();

        TenantContext.setOrganizationId(organizationId);

        metric = EmployeePerformanceMetric.builder()
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .userId(userId)
                .tasksAssigned(10)
                .tasksCompleted(9)
                .tasksOverdue(1)
                .storyPointsDelivered(27)
                .onTimeCompletionRate(new BigDecimal("90.00"))
                .performanceScore(new BigDecimal("90.00"))
                .performanceRating(PerformanceRating.EXCELLENT)
                .build();

        scorecardResponse = EmployeeScorecardResponse.builder()
                .userId(userId)
                .tasksAssigned(10)
                .tasksCompleted(9)
                .tasksOverdue(1)
                .storyPointsDelivered(27)
                .onTimeCompletionRate(new BigDecimal("90.00"))
                .performanceScore(new BigDecimal("90.00"))
                .performanceRating(PerformanceRating.EXCELLENT)
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should get employee scorecard successfully")
    void testGetEmployeeScorecard_Success() {
        when(employeePerformanceRepository.findByOrganizationIdAndUserId(organizationId, userId))
                .thenReturn(Optional.of(metric));
        when(analyticsMapper.toResponse(metric)).thenReturn(scorecardResponse);

        EmployeeScorecardResponse result = performanceService.getEmployeeScorecard(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getPerformanceScore()).isEqualByComparingTo(new BigDecimal("90.00"));
        assertThat(result.getPerformanceRating()).isEqualTo(PerformanceRating.EXCELLENT);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when scorecard not found")
    void testGetEmployeeScorecard_NotFound_ThrowsNotFound() {
        when(employeePerformanceRepository.findByOrganizationIdAndUserId(organizationId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> performanceService.getEmployeeScorecard(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee performance scorecard not found for user ID");
    }

    @Test
    @DisplayName("Should throw BadRequestException when TenantContext is missing")
    void testGetEmployeeScorecard_MissingTenant_ThrowsBadRequest() {
        TenantContext.clear();

        assertThatThrownBy(() -> performanceService.getEmployeeScorecard(userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Missing X-Organization-Id header");
    }

    @Test
    @DisplayName("Should get top performers leaderboard with limit")
    void testGetLeaderboard_Success() {
        when(employeePerformanceRepository.findByOrganizationIdOrderByPerformanceScoreDesc(organizationId))
                .thenReturn(List.of(metric));
        when(analyticsMapper.toResponse(metric)).thenReturn(scorecardResponse);

        List<EmployeeScorecardResponse> result = performanceService.getLeaderboard(5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPerformanceScore()).isEqualByComparingTo(new BigDecimal("90.00"));
    }

    @Test
    @DisplayName("Should get all employee scorecards")
    void testGetAllEmployeeScorecards_Success() {
        when(employeePerformanceRepository.findByOrganizationId(organizationId))
                .thenReturn(List.of(metric));
        when(analyticsMapper.toResponse(metric)).thenReturn(scorecardResponse);

        List<EmployeeScorecardResponse> result = performanceService.getAllEmployeeScorecards();

        assertThat(result).hasSize(1);
    }
}

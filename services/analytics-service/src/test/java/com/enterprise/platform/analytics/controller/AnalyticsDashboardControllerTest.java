package com.enterprise.platform.analytics.controller;

import com.enterprise.platform.analytics.dto.response.OrganizationDashboardResponse;
import com.enterprise.platform.analytics.dto.response.ProjectAnalyticsResponse;
import com.enterprise.platform.analytics.enums.ProjectHealthStatus;
import com.enterprise.platform.analytics.exception.GlobalExceptionHandler;
import com.enterprise.platform.analytics.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnalyticsDashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsDashboardController dashboardController;

    private UUID organizationId;
    private OrganizationDashboardResponse dashboardResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        organizationId = UUID.randomUUID();

        dashboardResponse = OrganizationDashboardResponse.builder()
                .organizationId(organizationId)
                .totalProjects(3)
                .totalTasks(50)
                .completedTasks(40)
                .overallCompletionRate(new BigDecimal("80.00"))
                .averageSprintVelocity(new BigDecimal("25.00"))
                .topPerformers(Collections.emptyList())
                .projectSummaries(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/analytics/dashboard - Should return organization dashboard")
    void testGetDashboard() throws Exception {
        when(analyticsService.getOrganizationDashboard()).thenReturn(dashboardResponse);

        mockMvc.perform(get("/api/v1/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.organizationId").value(organizationId.toString()))
                .andExpect(jsonPath("$.data.totalProjects").value(3))
                .andExpect(jsonPath("$.data.totalTasks").value(50));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/projects - Should return all projects analytics")
    void testGetAllProjectsAnalytics() throws Exception {
        ProjectAnalyticsResponse projResponse = ProjectAnalyticsResponse.builder()
                .projectId(UUID.randomUUID())
                .totalTasks(10)
                .completedTasks(8)
                .completionRate(new BigDecimal("80.00"))
                .healthStatus(ProjectHealthStatus.ON_TRACK)
                .build();

        when(analyticsService.getAllProjectsAnalytics()).thenReturn(List.of(projResponse));

        mockMvc.perform(get("/api/v1/analytics/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].totalTasks").value(10));
    }
}

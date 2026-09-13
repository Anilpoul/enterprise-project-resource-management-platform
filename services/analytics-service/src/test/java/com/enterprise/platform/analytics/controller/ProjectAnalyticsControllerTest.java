package com.enterprise.platform.analytics.controller;

import com.enterprise.platform.analytics.dto.response.ProjectAnalyticsResponse;
import com.enterprise.platform.analytics.dto.response.SprintAnalyticsResponse;
import com.enterprise.platform.analytics.enums.ProjectHealthStatus;
import com.enterprise.platform.analytics.exception.GlobalExceptionHandler;
import com.enterprise.platform.analytics.exception.ResourceNotFoundException;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProjectAnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private ProjectAnalyticsController projectAnalyticsController;

    private UUID projectId;
    private ProjectAnalyticsResponse projectResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectAnalyticsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        projectId = UUID.randomUUID();

        projectResponse = ProjectAnalyticsResponse.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .totalTasks(25)
                .completedTasks(20)
                .inProgressTasks(3)
                .blockedTasks(2)
                .overdueTasks(1)
                .completionRate(new BigDecimal("80.00"))
                .healthStatus(ProjectHealthStatus.ON_TRACK)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/analytics/projects/{projectId} - Should return project analytics")
    void testGetProjectAnalytics_Success() throws Exception {
        when(analyticsService.getProjectAnalytics(projectId)).thenReturn(projectResponse);

        mockMvc.perform(get("/api/v1/analytics/projects/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.data.totalTasks").value(25))
                .andExpect(jsonPath("$.data.completionRate").value(80.00));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/projects/{projectId} - Should return 404 when not found")
    void testGetProjectAnalytics_NotFound() throws Exception {
        when(analyticsService.getProjectAnalytics(projectId))
                .thenThrow(new ResourceNotFoundException("Project analytics not found for project ID: " + projectId));

        mockMvc.perform(get("/api/v1/analytics/projects/{projectId}", projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Project analytics not found for project ID: " + projectId));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/projects/{projectId}/sprints - Should return sprint analytics for project")
    void testGetProjectSprintsAnalytics() throws Exception {
        SprintAnalyticsResponse sprintResponse = SprintAnalyticsResponse.builder()
                .sprintId(UUID.randomUUID())
                .projectId(projectId)
                .sprintName("Sprint 1")
                .velocity(new BigDecimal("24.00"))
                .completionRate(new BigDecimal("92.00"))
                .status("COMPLETED")
                .build();

        when(analyticsService.getProjectSprintsAnalytics(projectId)).thenReturn(List.of(sprintResponse));

        mockMvc.perform(get("/api/v1/analytics/projects/{projectId}/sprints", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].sprintName").value("Sprint 1"));
    }
}

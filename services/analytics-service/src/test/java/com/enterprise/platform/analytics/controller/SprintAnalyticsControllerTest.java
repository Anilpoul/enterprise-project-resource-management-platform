package com.enterprise.platform.analytics.controller;

import com.enterprise.platform.analytics.dto.response.SprintAnalyticsResponse;
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
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SprintAnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private SprintAnalyticsController sprintAnalyticsController;

    private UUID sprintId;
    private SprintAnalyticsResponse sprintResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sprintAnalyticsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sprintId = UUID.randomUUID();

        sprintResponse = SprintAnalyticsResponse.builder()
                .id(UUID.randomUUID())
                .sprintId(sprintId)
                .projectId(UUID.randomUUID())
                .sprintName("Sprint 1")
                .committedStoryPoints(30)
                .completedStoryPoints(27)
                .velocity(new BigDecimal("27.00"))
                .completionRate(new BigDecimal("90.00"))
                .spilloverTasksCount(1)
                .status("COMPLETED")
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/analytics/sprints/{sprintId} - Should return sprint analytics")
    void testGetSprintAnalytics_Success() throws Exception {
        when(analyticsService.getSprintAnalytics(sprintId)).thenReturn(sprintResponse);

        mockMvc.perform(get("/api/v1/analytics/sprints/{sprintId}", sprintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sprintId").value(sprintId.toString()))
                .andExpect(jsonPath("$.data.sprintName").value("Sprint 1"))
                .andExpect(jsonPath("$.data.velocity").value(27.00));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/sprints/{sprintId} - Should return 404 when not found")
    void testGetSprintAnalytics_NotFound() throws Exception {
        when(analyticsService.getSprintAnalytics(sprintId))
                .thenThrow(new ResourceNotFoundException("Sprint analytics not found for sprint ID: " + sprintId));

        mockMvc.perform(get("/api/v1/analytics/sprints/{sprintId}", sprintId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Sprint analytics not found for sprint ID: " + sprintId));
    }
}

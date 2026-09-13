package com.enterprise.platform.analytics.controller;

import com.enterprise.platform.analytics.dto.response.EmployeeScorecardResponse;
import com.enterprise.platform.analytics.enums.PerformanceRating;
import com.enterprise.platform.analytics.exception.GlobalExceptionHandler;
import com.enterprise.platform.analytics.exception.ResourceNotFoundException;
import com.enterprise.platform.analytics.service.PerformanceService;
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
class EmployeePerformanceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PerformanceService performanceService;

    @InjectMocks
    private EmployeePerformanceController performanceController;

    private UUID userId;
    private EmployeeScorecardResponse scorecardResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(performanceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userId = UUID.randomUUID();

        scorecardResponse = EmployeeScorecardResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .tasksAssigned(15)
                .tasksCompleted(14)
                .tasksOverdue(1)
                .storyPointsDelivered(42)
                .onTimeCompletionRate(new BigDecimal("93.33"))
                .performanceScore(new BigDecimal("93.33"))
                .performanceRating(PerformanceRating.EXCELLENT)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/analytics/employees/{userId} - Should return employee scorecard")
    void testGetEmployeeScorecard_Success() throws Exception {
        when(performanceService.getEmployeeScorecard(userId)).thenReturn(scorecardResponse);

        mockMvc.perform(get("/api/v1/analytics/employees/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.tasksCompleted").value(14))
                .andExpect(jsonPath("$.data.performanceScore").value(93.33));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/employees/{userId} - Should return 404 when not found")
    void testGetEmployeeScorecard_NotFound() throws Exception {
        when(performanceService.getEmployeeScorecard(userId))
                .thenThrow(new ResourceNotFoundException("Employee performance scorecard not found for user ID: " + userId));

        mockMvc.perform(get("/api/v1/analytics/employees/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Employee performance scorecard not found for user ID: " + userId));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/employees/leaderboard - Should return top performers")
    void testGetLeaderboard() throws Exception {
        when(performanceService.getLeaderboard(10)).thenReturn(List.of(scorecardResponse));

        mockMvc.perform(get("/api/v1/analytics/employees/leaderboard")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].performanceRating").value("EXCELLENT"));
    }

    @Test
    @DisplayName("GET /api/v1/analytics/employees - Should return all employee scorecards")
    void testGetAllEmployeeScorecards() throws Exception {
        when(performanceService.getAllEmployeeScorecards()).thenReturn(List.of(scorecardResponse));

        mockMvc.perform(get("/api/v1/analytics/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value(userId.toString()));
    }
}

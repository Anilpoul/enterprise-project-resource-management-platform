package com.enterprise.platform.sprint.controller;

import com.enterprise.platform.sprint.constants.enums.SprintStatus;
import com.enterprise.platform.sprint.dto.request.CompleteSprintRequest;
import com.enterprise.platform.sprint.dto.request.CreateSprintRequest;
import com.enterprise.platform.sprint.dto.request.StartSprintRequest;
import com.enterprise.platform.sprint.dto.request.UpdateSprintRequest;
import com.enterprise.platform.sprint.dto.response.SprintResponse;
import com.enterprise.platform.sprint.exception.GlobalExceptionHandler;
import com.enterprise.platform.sprint.exception.ResourceNotFoundException;
import com.enterprise.platform.sprint.service.SprintService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SprintControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SprintService sprintService;

    @InjectMocks
    private SprintController sprintController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID sprintId;
    private UUID projectId;
    private SprintResponse sprintResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sprintController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sprintId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        sprintResponse = SprintResponse.builder()
                .id(sprintId)
                .organizationId(UUID.randomUUID())
                .projectId(projectId)
                .name("Sprint 1 - Foundation")
                .goal("Initial APIs")
                .status(SprintStatus.FUTURE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(2))
                .totalStoryPoints(20)
                .completedStoryPoints(0)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/sprints - Success returns 201 CREATED")
    void testCreateSprint_Success() throws Exception {
        CreateSprintRequest request = CreateSprintRequest.builder()
                .projectId(projectId)
                .name("Sprint 1 - Foundation")
                .goal("Initial APIs")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(2))
                .build();

        when(sprintService.createSprint(any(CreateSprintRequest.class))).thenReturn(sprintResponse);

        mockMvc.perform(post("/api/v1/sprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Sprint 1 - Foundation"))
                .andExpect(jsonPath("$.data.status").value("FUTURE"));
    }

    @Test
    @DisplayName("POST /api/v1/sprints - Validation failure returns 400")
    void testCreateSprint_ValidationFailure() throws Exception {
        CreateSprintRequest request = CreateSprintRequest.builder()
                .projectId(null)
                .name("")
                .build();

        mockMvc.perform(post("/api/v1/sprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/sprints/{id}/start - Success returns 200 OK")
    void testStartSprint_Success() throws Exception {
        StartSprintRequest request = StartSprintRequest.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(2))
                .build();

        SprintResponse activeResponse = SprintResponse.builder()
                .id(sprintId)
                .status(SprintStatus.ACTIVE)
                .build();

        when(sprintService.startSprint(eq(sprintId), any(StartSprintRequest.class))).thenReturn(activeResponse);

        mockMvc.perform(post("/api/v1/sprints/{id}/start", sprintId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/sprints/{id}/complete - Success returns 200 OK")
    void testCompleteSprint_Success() throws Exception {
        CompleteSprintRequest request = CompleteSprintRequest.builder()
                .totalStoryPoints(20)
                .completedStoryPoints(20)
                .build();

        SprintResponse closedResponse = SprintResponse.builder()
                .id(sprintId)
                .status(SprintStatus.CLOSED)
                .build();

        when(sprintService.completeSprint(eq(sprintId), any(CompleteSprintRequest.class))).thenReturn(closedResponse);

        mockMvc.perform(post("/api/v1/sprints/{id}/complete", sprintId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    @Test
    @DisplayName("GET /api/v1/sprints/{id} - Success returns 200 OK")
    void testGetSprintById_Success() throws Exception {
        when(sprintService.getSprintById(sprintId)).thenReturn(sprintResponse);

        mockMvc.perform(get("/api/v1/sprints/{id}", sprintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Sprint 1 - Foundation"));
    }

    @Test
    @DisplayName("GET /api/v1/sprints/{id} - Not found returns 404")
    void testGetSprintById_NotFound() throws Exception {
        when(sprintService.getSprintById(sprintId))
                .thenThrow(new ResourceNotFoundException("Sprint not found with ID: " + sprintId));

        mockMvc.perform(get("/api/v1/sprints/{id}", sprintId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/sprints - Success returns 200 list")
    void testGetSprintsByProject_Success() throws Exception {
        when(sprintService.getSprintsByProject(eq(projectId), any())).thenReturn(List.of(sprintResponse));

        mockMvc.perform(get("/api/v1/projects/{projectId}/sprints", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Sprint 1 - Foundation"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/sprints/active - Success returns 200 OK")
    void testGetActiveSprint_Success() throws Exception {
        when(sprintService.getActiveSprint(projectId)).thenReturn(sprintResponse);

        mockMvc.perform(get("/api/v1/projects/{projectId}/sprints/active", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/sprints/{id} - Success returns 200 OK")
    void testUpdateSprint_Success() throws Exception {
        UpdateSprintRequest request = UpdateSprintRequest.builder()
                .name("Updated Sprint Name")
                .build();

        when(sprintService.updateSprint(eq(sprintId), any(UpdateSprintRequest.class))).thenReturn(sprintResponse);

        mockMvc.perform(put("/api/v1/sprints/{id}", sprintId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/sprints/{id} - Success returns 200 OK")
    void testDeleteSprint_Success() throws Exception {
        doNothing().when(sprintService).deleteSprint(sprintId);

        mockMvc.perform(delete("/api/v1/sprints/{id}", sprintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sprint deleted successfully"));
    }
}

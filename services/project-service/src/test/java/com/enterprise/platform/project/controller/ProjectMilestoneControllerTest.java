package com.enterprise.platform.project.controller;

import com.enterprise.platform.project.constants.enums.MilestoneStatus;
import com.enterprise.platform.project.dto.request.CreateMilestoneRequest;
import com.enterprise.platform.project.dto.request.UpdateMilestoneRequest;
import com.enterprise.platform.project.dto.response.ProjectMilestoneResponse;
import com.enterprise.platform.project.exception.GlobalExceptionHandler;
import com.enterprise.platform.project.exception.ResourceNotFoundException;
import com.enterprise.platform.project.service.ProjectMilestoneService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProjectMilestoneControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectMilestoneService milestoneService;

    @InjectMocks
    private ProjectMilestoneController milestoneController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID projectId;
    private UUID milestoneId;
    private ProjectMilestoneResponse milestoneResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(milestoneController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        projectId = UUID.randomUUID();
        milestoneId = UUID.randomUUID();

        milestoneResponse = ProjectMilestoneResponse.builder()
                .id(milestoneId)
                .projectId(projectId)
                .organizationId(UUID.randomUUID())
                .name("Release 1.0")
                .description("Production deployment milestone")
                .dueDate(LocalDate.now().plusMonths(1))
                .status(MilestoneStatus.OPEN)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/projects/{projectId}/milestones - Success returns 201 CREATED")
    void testCreateMilestone_Success() throws Exception {
        CreateMilestoneRequest request = new CreateMilestoneRequest();
        request.setName("Release 1.0");
        request.setDescription("Production deployment milestone");
        request.setDueDate(LocalDate.now().plusMonths(1));
        request.setStatus(MilestoneStatus.OPEN);

        when(milestoneService.createMilestone(eq(projectId), any(CreateMilestoneRequest.class)))
                .thenReturn(milestoneResponse);

        mockMvc.perform(post("/api/v1/projects/{projectId}/milestones", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Release 1.0"))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{projectId}/milestones - Blank name returns 400")
    void testCreateMilestone_ValidationFailure() throws Exception {
        CreateMilestoneRequest request = new CreateMilestoneRequest();
        request.setName(""); // Blank

        mockMvc.perform(post("/api/v1/projects/{projectId}/milestones", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/milestones - Success returns 200 list")
    void testGetMilestones_Success() throws Exception {
        when(milestoneService.getMilestones(projectId)).thenReturn(List.of(milestoneResponse));

        mockMvc.perform(get("/api/v1/projects/{projectId}/milestones", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Release 1.0"));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{projectId}/milestones/{milestoneId} - Success returns 200")
    void testUpdateMilestone_Success() throws Exception {
        UpdateMilestoneRequest request = new UpdateMilestoneRequest();
        request.setName("Release 1.1");
        request.setStatus(MilestoneStatus.IN_PROGRESS);

        ProjectMilestoneResponse updated = ProjectMilestoneResponse.builder()
                .id(milestoneId)
                .projectId(projectId)
                .name("Release 1.1")
                .status(MilestoneStatus.IN_PROGRESS)
                .build();

        when(milestoneService.updateMilestone(eq(projectId), eq(milestoneId), any(UpdateMilestoneRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/projects/{projectId}/milestones/{milestoneId}", projectId, milestoneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Release 1.1"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{projectId}/milestones/{milestoneId} - Not found returns 404")
    void testUpdateMilestone_NotFound() throws Exception {
        UpdateMilestoneRequest request = new UpdateMilestoneRequest();
        request.setName("Release 1.1");

        when(milestoneService.updateMilestone(eq(projectId), eq(milestoneId), any(UpdateMilestoneRequest.class)))
                .thenThrow(new ResourceNotFoundException("Milestone not found with ID: " + milestoneId));

        mockMvc.perform(put("/api/v1/projects/{projectId}/milestones/{milestoneId}", projectId, milestoneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("DELETE /api/v1/projects/{projectId}/milestones/{milestoneId} - Success returns 200")
    void testDeleteMilestone_Success() throws Exception {
        doNothing().when(milestoneService).deleteMilestone(projectId, milestoneId);

        mockMvc.perform(delete("/api/v1/projects/{projectId}/milestones/{milestoneId}", projectId, milestoneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Milestone deleted successfully"));
    }
}

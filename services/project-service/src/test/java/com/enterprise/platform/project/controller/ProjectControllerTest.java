package com.enterprise.platform.project.controller;

import com.enterprise.platform.project.constants.enums.ProjectStatus;
import com.enterprise.platform.project.constants.enums.ProjectType;
import com.enterprise.platform.project.constants.enums.ProjectVisibility;
import com.enterprise.platform.project.dto.request.CreateProjectRequest;
import com.enterprise.platform.project.dto.request.ProjectSearchCriteria;
import com.enterprise.platform.project.dto.request.UpdateProjectRequest;
import com.enterprise.platform.project.dto.response.PagedResponse;
import com.enterprise.platform.project.dto.response.ProjectResponse;
import com.enterprise.platform.project.exception.ConflictException;
import com.enterprise.platform.project.exception.GlobalExceptionHandler;
import com.enterprise.platform.project.exception.ResourceNotFoundException;
import com.enterprise.platform.project.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
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
class ProjectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID projectId;
    private UUID organizationId;
    private ProjectResponse projectResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        projectId = UUID.randomUUID();
        organizationId = UUID.randomUUID();

        projectResponse = ProjectResponse.builder()
                .id(projectId)
                .organizationId(organizationId)
                .name("Core Platform")
                .projectKey("CORE")
                .description("Enterprise core platform")
                .projectType(ProjectType.SOFTWARE)
                .status(ProjectStatus.ACTIVE)
                .visibility(ProjectVisibility.PUBLIC)
                .leadUserId(UUID.randomUUID())
                .budget(BigDecimal.valueOf(150000))
                .currency("USD")
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/projects - Success returns 201 CREATED")
    void testCreateProject_Success() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Core Platform");
        request.setProjectKey("CORE");
        request.setDescription("Enterprise core platform");
        request.setProjectType(ProjectType.SOFTWARE);
        request.setVisibility(ProjectVisibility.PUBLIC);
        request.setLeadUserId(UUID.randomUUID());
        request.setBudget(BigDecimal.valueOf(150000));
        request.setCurrency("USD");

        when(projectService.createProject(any(CreateProjectRequest.class))).thenReturn(projectResponse);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Core Platform"))
                .andExpect(jsonPath("$.data.projectKey").value("CORE"));
    }

    @Test
    @DisplayName("POST /api/v1/projects - Validation failure returns 400")
    void testCreateProject_ValidationFailure() throws Exception {
        CreateProjectRequest invalidRequest = new CreateProjectRequest();
        invalidRequest.setName(""); // Blank
        invalidRequest.setProjectKey("invalid key with spaces");

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/projects - Conflict returns 409")
    void testCreateProject_Conflict() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Core Platform");
        request.setProjectKey("CORE");
        request.setProjectType(ProjectType.SOFTWARE);
        request.setLeadUserId(UUID.randomUUID());

        when(projectService.createProject(any(CreateProjectRequest.class)))
                .thenThrow(new ConflictException("Project key already exists in organization"));

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Project key already exists in organization"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id} - Success returns 200 OK")
    void testGetProjectById_Success() throws Exception {
        when(projectService.getProjectById(projectId)).thenReturn(projectResponse);

        mockMvc.perform(get("/api/v1/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(projectId.toString()))
                .andExpect(jsonPath("$.data.name").value("Core Platform"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{id} - Not found returns 404")
    void testGetProjectById_NotFound() throws Exception {
        when(projectService.getProjectById(projectId))
                .thenThrow(new ResourceNotFoundException("Project not found with ID: " + projectId));

        mockMvc.perform(get("/api/v1/projects/{id}", projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/projects/key/{projectKey} - Success returns 200 OK")
    void testGetProjectByKey_Success() throws Exception {
        when(projectService.getProjectByKey("CORE")).thenReturn(projectResponse);

        mockMvc.perform(get("/api/v1/projects/key/{projectKey}", "CORE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectKey").value("CORE"));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id} - Success returns 200 OK")
    void testUpdateProject_Success() throws Exception {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("Core Platform v2");
        request.setDescription("Updated description");

        when(projectService.updateProject(eq(projectId), any(UpdateProjectRequest.class))).thenReturn(projectResponse);

        mockMvc.perform(put("/api/v1/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Project updated successfully"));
    }

    @Test
    @DisplayName("PATCH /api/v1/projects/{id}/status - Success returns 200 OK")
    void testUpdateProjectStatus_Success() throws Exception {
        when(projectService.updateProjectStatus(projectId, ProjectStatus.COMPLETED)).thenReturn(projectResponse);

        mockMvc.perform(patch("/api/v1/projects/{id}/status", projectId)
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Project status updated successfully"));
    }

    @Test
    @DisplayName("GET /api/v1/projects - Search returns 200 OK with PagedResponse")
    void testSearchProjects_Success() throws Exception {
        PagedResponse<ProjectResponse> pagedResponse = PagedResponse.<ProjectResponse>builder()
                .content(List.of(projectResponse))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(projectService.searchProjects(any(ProjectSearchCriteria.class), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/projects")
                        .param("search", "Core")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Core Platform"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("DELETE /api/v1/projects/{id} - Success returns 200 OK")
    void testDeleteProject_Success() throws Exception {
        doNothing().when(projectService).deleteProject(projectId);

        mockMvc.perform(delete("/api/v1/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Project archived successfully"));
    }
}

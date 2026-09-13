package com.enterprise.platform.project.controller;

import com.enterprise.platform.project.constants.enums.MemberStatus;
import com.enterprise.platform.project.constants.enums.ProjectRole;
import com.enterprise.platform.project.dto.request.AddProjectMemberRequest;
import com.enterprise.platform.project.dto.request.UpdateProjectMemberRoleRequest;
import com.enterprise.platform.project.dto.response.PagedResponse;
import com.enterprise.platform.project.dto.response.ProjectMemberResponse;
import com.enterprise.platform.project.exception.ConflictException;
import com.enterprise.platform.project.exception.GlobalExceptionHandler;
import com.enterprise.platform.project.exception.ResourceNotFoundException;
import com.enterprise.platform.project.service.ProjectMemberService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectMemberService projectMemberService;

    @InjectMocks
    private ProjectMemberController projectMemberController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID projectId;
    private UUID userId;
    private ProjectMemberResponse memberResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectMemberController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();

        memberResponse = ProjectMemberResponse.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .organizationId(UUID.randomUUID())
                .userId(userId)
                .role(ProjectRole.DEVELOPER)
                .status(MemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/projects/{projectId}/members - Success returns 201 CREATED")
    void testAddMember_Success() throws Exception {
        AddProjectMemberRequest request = new AddProjectMemberRequest();
        request.setUserId(userId);
        request.setRole(ProjectRole.DEVELOPER);

        when(projectMemberService.addMember(eq(projectId), any(AddProjectMemberRequest.class)))
                .thenReturn(memberResponse);

        mockMvc.perform(post("/api/v1/projects/{projectId}/members", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.role").value("DEVELOPER"));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{projectId}/members - Validation failure returns 400")
    void testAddMember_ValidationFailure() throws Exception {
        AddProjectMemberRequest invalidRequest = new AddProjectMemberRequest();
        // Missing userId and role

        mockMvc.perform(post("/api/v1/projects/{projectId}/members", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{projectId}/members - Already member returns 409")
    void testAddMember_Conflict() throws Exception {
        AddProjectMemberRequest request = new AddProjectMemberRequest();
        request.setUserId(userId);
        request.setRole(ProjectRole.DEVELOPER);

        when(projectMemberService.addMember(eq(projectId), any(AddProjectMemberRequest.class)))
                .thenThrow(new ConflictException("User is already a member of this project"));

        mockMvc.perform(post("/api/v1/projects/{projectId}/members", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is already a member of this project"));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/members - Success returns 200 with PagedResponse")
    void testGetMembers_Success() throws Exception {
        PagedResponse<ProjectMemberResponse> pagedResponse = PagedResponse.<ProjectMemberResponse>builder()
                .content(List.of(memberResponse))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(projectMemberService.getMembers(eq(projectId), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/projects/{projectId}/members", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/members/{userId} - Success returns 200")
    void testGetMember_Success() throws Exception {
        when(projectMemberService.getMember(projectId, userId)).thenReturn(memberResponse);

        mockMvc.perform(get("/api/v1/projects/{projectId}/members/{userId}", projectId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/projects/{projectId}/members/{userId} - Not found returns 404")
    void testGetMember_NotFound() throws Exception {
        when(projectMemberService.getMember(projectId, userId))
                .thenThrow(new ResourceNotFoundException("Member not found in project"));

        mockMvc.perform(get("/api/v1/projects/{projectId}/members/{userId}", projectId, userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{projectId}/members/{userId}/role - Success returns 200")
    void testUpdateMemberRole_Success() throws Exception {
        UpdateProjectMemberRoleRequest request = new UpdateProjectMemberRoleRequest();
        request.setRole(ProjectRole.PRODUCT_OWNER);

        ProjectMemberResponse updated = ProjectMemberResponse.builder()
                .id(memberResponse.getId())
                .projectId(projectId)
                .userId(userId)
                .role(ProjectRole.PRODUCT_OWNER)
                .status(MemberStatus.ACTIVE)
                .build();

        when(projectMemberService.updateMemberRole(eq(projectId), eq(userId), any(UpdateProjectMemberRoleRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/projects/{projectId}/members/{userId}/role", projectId, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("PRODUCT_OWNER"));
    }

    @Test
    @DisplayName("DELETE /api/v1/projects/{projectId}/members/{userId} - Success returns 200")
    void testRemoveMember_Success() throws Exception {
        doNothing().when(projectMemberService).removeMember(projectId, userId);

        mockMvc.perform(delete("/api/v1/projects/{projectId}/members/{userId}", projectId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Member removed successfully"));
    }
}

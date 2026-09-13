package com.enterprise.platform.project.service.impl;

import com.enterprise.platform.events.ProjectEvent;
import com.enterprise.platform.events.ProjectEventType;
import com.enterprise.platform.project.constants.enums.ProjectRole;
import com.enterprise.platform.project.constants.enums.ProjectStatus;
import com.enterprise.platform.project.constants.enums.ProjectType;
import com.enterprise.platform.project.constants.enums.ProjectVisibility;
import com.enterprise.platform.project.context.TenantContext;
import com.enterprise.platform.project.dto.request.CreateProjectRequest;
import com.enterprise.platform.project.dto.request.ProjectSearchCriteria;
import com.enterprise.platform.project.dto.request.UpdateProjectRequest;
import com.enterprise.platform.project.dto.response.PagedResponse;
import com.enterprise.platform.project.dto.response.ProjectResponse;
import com.enterprise.platform.project.entity.Project;
import com.enterprise.platform.project.exception.ConflictException;
import com.enterprise.platform.project.exception.ResourceNotFoundException;
import com.enterprise.platform.project.kafka.producer.ProjectEventProducer;
import com.enterprise.platform.project.mapper.ProjectMapper;
import com.enterprise.platform.project.repository.ProjectMemberRepository;
import com.enterprise.platform.project.repository.ProjectMilestoneRepository;
import com.enterprise.platform.project.repository.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectMilestoneRepository projectMilestoneRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectEventProducer eventProducer;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private UUID organizationId;
    private UUID projectId;
    private UUID leadUserId;
    private Project project;
    private ProjectResponse projectResponse;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        leadUserId = UUID.randomUUID();

        TenantContext.setOrganizationId(organizationId);
        TenantContext.setUserId(leadUserId);

        project = new Project();
        project.setId(projectId);
        project.setOrganizationId(organizationId);
        project.setName("Core Platform");
        project.setProjectKey("CORE");
        project.setDescription("Core platform project");
        project.setProjectType(ProjectType.SOFTWARE);
        project.setStatus(ProjectStatus.ACTIVE);
        project.setVisibility(ProjectVisibility.PUBLIC);
        project.setLeadUserId(leadUserId);
        project.setCurrency("USD");

        projectResponse = ProjectResponse.builder()
                .id(projectId)
                .organizationId(organizationId)
                .name("Core Platform")
                .projectKey("CORE")
                .description("Core platform project")
                .projectType(ProjectType.SOFTWARE)
                .status(ProjectStatus.ACTIVE)
                .visibility(ProjectVisibility.PUBLIC)
                .leadUserId(leadUserId)
                .currency("USD")
                .memberCount(1L)
                .milestoneCount(0L)
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Create project successfully assigns lead member and publishes event")
    void testCreateProject_Success() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Core Platform")
                .projectKey("CORE")
                .description("Core platform project")
                .projectType(ProjectType.SOFTWARE)
                .leadUserId(leadUserId)
                .budget(BigDecimal.valueOf(50000))
                .build();

        when(projectRepository.existsByOrganizationIdAndProjectKey(organizationId, "CORE")).thenReturn(false);
        when(projectRepository.existsByOrganizationIdAndName(organizationId, "Core Platform")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(projectId);
            return p;
        });
        when(projectMapper.toResponse(any(Project.class))).thenReturn(projectResponse);

        ProjectResponse result = projectService.createProject(request);

        assertThat(result).isNotNull();
        assertThat(result.getProjectKey()).isEqualTo("CORE");
        verify(projectRepository).save(any(Project.class));

        ArgumentCaptor<ProjectEvent> captor = ArgumentCaptor.forClass(ProjectEvent.class);
        verify(eventProducer).publish(captor.capture());
        ProjectEvent event = captor.getValue();
        assertThat(event.getEventType()).isEqualTo(ProjectEventType.PROJECT_CREATED);
        assertThat(event.getProjectKey()).isEqualTo("CORE");
        assertThat(event.getUserId()).isEqualTo(leadUserId);
        assertThat(event.getMemberRole()).isEqualTo(ProjectRole.PROJECT_LEAD.name());
    }

    @Test
    @DisplayName("Create project throws ConflictException when key already exists")
    void testCreateProject_KeyConflict() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Core Platform")
                .projectKey("CORE")
                .projectType(ProjectType.SOFTWARE)
                .leadUserId(leadUserId)
                .build();

        when(projectRepository.existsByOrganizationIdAndProjectKey(organizationId, "CORE")).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(projectRepository, never()).save(any());
        verify(eventProducer, never()).publish(any());
    }

    @Test
    @DisplayName("Create project throws ConflictException when name already exists")
    void testCreateProject_NameConflict() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Core Platform")
                .projectKey("CORE")
                .projectType(ProjectType.SOFTWARE)
                .leadUserId(leadUserId)
                .build();

        when(projectRepository.existsByOrganizationIdAndProjectKey(organizationId, "CORE")).thenReturn(false);
        when(projectRepository.existsByOrganizationIdAndName(organizationId, "Core Platform")).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get project by ID successfully with member and milestone counts")
    void testGetProjectById_Success() {
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);
        when(projectMemberRepository.countByProjectId(projectId)).thenReturn(3L);
        when(projectMilestoneRepository.findByProjectId(projectId)).thenReturn(List.of());

        ProjectResponse result = projectService.getProjectById(projectId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(projectId);
        assertThat(result.getMemberCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Get project by ID throws ResourceNotFoundException when not found")
    void testGetProjectById_NotFound() {
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(projectId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Get project by key successfully")
    void testGetProjectByKey_Success() {
        when(projectRepository.findByOrganizationIdAndProjectKey(organizationId, "CORE")).thenReturn(Optional.of(project));
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);
        when(projectMemberRepository.countByProjectId(projectId)).thenReturn(1L);
        when(projectMilestoneRepository.findByProjectId(projectId)).thenReturn(List.of());

        ProjectResponse result = projectService.getProjectByKey("core");

        assertThat(result).isNotNull();
        assertThat(result.getProjectKey()).isEqualTo("CORE");
    }

    @Test
    @DisplayName("Update project details successfully")
    void testUpdateProject_Success() {
        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .name("Core Platform Renamed")
                .description("Updated description")
                .build();

        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(projectRepository.existsByOrganizationIdAndName(organizationId, "Core Platform Renamed")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);
        when(projectMemberRepository.countByProjectId(projectId)).thenReturn(1L);
        when(projectMilestoneRepository.findByProjectId(projectId)).thenReturn(List.of());

        ProjectResponse result = projectService.updateProject(projectId, request);

        assertThat(result).isNotNull();
        verify(projectRepository).save(project);
        verify(eventProducer).publish(any(ProjectEvent.class));
    }

    @Test
    @DisplayName("Update project status publishes status changed event")
    void testUpdateProjectStatus_Success() {
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);
        when(projectMemberRepository.countByProjectId(projectId)).thenReturn(1L);
        when(projectMilestoneRepository.findByProjectId(projectId)).thenReturn(List.of());

        projectService.updateProjectStatus(projectId, ProjectStatus.COMPLETED);

        ArgumentCaptor<ProjectEvent> captor = ArgumentCaptor.forClass(ProjectEvent.class);
        verify(eventProducer).publish(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(ProjectEventType.PROJECT_STATUS_CHANGED);
    }

    @Test
    @DisplayName("Search projects with criteria and pagination")
    void testSearchProjects_WithCriteria() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> page = new PageImpl<>(List.of(project), pageable, 1);

        when(projectRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);
        when(projectMemberRepository.countByProjectId(projectId)).thenReturn(1L);
        when(projectMilestoneRepository.findByProjectId(projectId)).thenReturn(List.of());

        ProjectSearchCriteria criteria = ProjectSearchCriteria.builder()
                .search("core")
                .status(ProjectStatus.ACTIVE)
                .build();

        PagedResponse<ProjectResponse> result = projectService.searchProjects(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Delete project archives it and publishes archived event")
    void testDeleteProject_Success() {
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));

        projectService.deleteProject(projectId);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ARCHIVED);
        verify(projectRepository).save(project);
        ArgumentCaptor<ProjectEvent> captor = ArgumentCaptor.forClass(ProjectEvent.class);
        verify(eventProducer).publish(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(ProjectEventType.PROJECT_ARCHIVED);
    }
}

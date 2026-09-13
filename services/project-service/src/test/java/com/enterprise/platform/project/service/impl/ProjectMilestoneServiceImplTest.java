package com.enterprise.platform.project.service.impl;

import com.enterprise.platform.project.constants.enums.MilestoneStatus;
import com.enterprise.platform.project.context.TenantContext;
import com.enterprise.platform.project.dto.request.CreateMilestoneRequest;
import com.enterprise.platform.project.dto.request.UpdateMilestoneRequest;
import com.enterprise.platform.project.dto.response.ProjectMilestoneResponse;
import com.enterprise.platform.project.entity.Project;
import com.enterprise.platform.project.entity.ProjectMilestone;
import com.enterprise.platform.project.exception.BadRequestException;
import com.enterprise.platform.project.exception.ResourceNotFoundException;
import com.enterprise.platform.project.mapper.ProjectMilestoneMapper;
import com.enterprise.platform.project.repository.ProjectMilestoneRepository;
import com.enterprise.platform.project.repository.ProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMilestoneServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMilestoneRepository milestoneRepository;

    @Mock
    private ProjectMilestoneMapper milestoneMapper;

    @InjectMocks
    private ProjectMilestoneServiceImpl milestoneService;

    private UUID organizationId;
    private UUID projectId;
    private UUID milestoneId;
    private Project project;
    private ProjectMilestone milestone;
    private ProjectMilestoneResponse milestoneResponse;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        milestoneId = UUID.randomUUID();

        TenantContext.setOrganizationId(organizationId);

        project = new Project();
        project.setId(projectId);
        project.setOrganizationId(organizationId);
        project.setName("Payments Service");
        project.setProjectKey("PAY");

        milestone = new ProjectMilestone();
        milestone.setId(milestoneId);
        milestone.setProject(project);
        milestone.setOrganizationId(organizationId);
        milestone.setName("Beta Release");
        milestone.setDescription("Initial customer rollout");
        milestone.setDueDate(LocalDate.now().plusMonths(2));
        milestone.setStatus(MilestoneStatus.OPEN);

        milestoneResponse = ProjectMilestoneResponse.builder()
                .id(milestoneId)
                .projectId(projectId)
                .organizationId(organizationId)
                .name("Beta Release")
                .description("Initial customer rollout")
                .dueDate(milestone.getDueDate())
                .status(MilestoneStatus.OPEN)
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should create milestone successfully")
    void testCreateMilestone_Success() {
        CreateMilestoneRequest request = new CreateMilestoneRequest();
        request.setName("Beta Release");
        request.setDescription("Initial customer rollout");
        request.setDueDate(LocalDate.now().plusMonths(2));
        request.setStatus(MilestoneStatus.OPEN);

        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(milestoneRepository.save(any(ProjectMilestone.class))).thenReturn(milestone);
        when(milestoneMapper.toResponse(milestone)).thenReturn(milestoneResponse);

        ProjectMilestoneResponse response = milestoneService.createMilestone(projectId, request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Beta Release");
        assertThat(response.getStatus()).isEqualTo(MilestoneStatus.OPEN);
        verify(milestoneRepository).save(any(ProjectMilestone.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project not found during milestone creation")
    void testCreateMilestone_ProjectNotFound() {
        CreateMilestoneRequest request = new CreateMilestoneRequest();
        request.setName("Beta Release");

        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> milestoneService.createMilestone(projectId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");

        verify(milestoneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException when tenant header is missing")
    void testCreateMilestone_MissingTenantHeader() {
        TenantContext.clear();
        CreateMilestoneRequest request = new CreateMilestoneRequest();
        request.setName("Beta Release");

        assertThatThrownBy(() -> milestoneService.createMilestone(projectId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Missing X-Organization-Id");
    }

    @Test
    @DisplayName("Should get all milestones for a project")
    void testGetMilestones_Success() {
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(milestoneRepository.findByProjectId(projectId)).thenReturn(List.of(milestone));
        when(milestoneMapper.toResponse(milestone)).thenReturn(milestoneResponse);

        List<ProjectMilestoneResponse> list = milestoneService.getMilestones(projectId);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("Beta Release");
    }

    @Test
    @DisplayName("Should update milestone successfully")
    void testUpdateMilestone_Success() {
        UpdateMilestoneRequest request = new UpdateMilestoneRequest();
        request.setName("GA Release");
        request.setStatus(MilestoneStatus.IN_PROGRESS);

        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(milestoneRepository.findByIdAndProjectId(milestoneId, projectId)).thenReturn(Optional.of(milestone));
        when(milestoneRepository.save(milestone)).thenReturn(milestone);

        ProjectMilestoneResponse updatedResponse = ProjectMilestoneResponse.builder()
                .id(milestoneId)
                .projectId(projectId)
                .organizationId(organizationId)
                .name("GA Release")
                .status(MilestoneStatus.IN_PROGRESS)
                .build();
        when(milestoneMapper.toResponse(milestone)).thenReturn(updatedResponse);

        ProjectMilestoneResponse result = milestoneService.updateMilestone(projectId, milestoneId, request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("GA Release");
        assertThat(result.getStatus()).isEqualTo(MilestoneStatus.IN_PROGRESS);
        verify(milestoneRepository).save(milestone);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent milestone")
    void testUpdateMilestone_NotFound() {
        UpdateMilestoneRequest request = new UpdateMilestoneRequest();
        request.setName("GA Release");

        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(milestoneRepository.findByIdAndProjectId(milestoneId, projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> milestoneService.updateMilestone(projectId, milestoneId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Milestone not found");

        verify(milestoneRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete milestone successfully")
    void testDeleteMilestone_Success() {
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(milestoneRepository.findByIdAndProjectId(milestoneId, projectId)).thenReturn(Optional.of(milestone));

        milestoneService.deleteMilestone(projectId, milestoneId);

        verify(milestoneRepository).delete(milestone);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent milestone")
    void testDeleteMilestone_NotFound() {
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(milestoneRepository.findByIdAndProjectId(milestoneId, projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> milestoneService.deleteMilestone(projectId, milestoneId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Milestone not found");

        verify(milestoneRepository, never()).delete(any());
    }
}

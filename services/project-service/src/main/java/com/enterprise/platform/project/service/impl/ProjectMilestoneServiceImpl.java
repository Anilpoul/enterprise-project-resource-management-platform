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
import com.enterprise.platform.project.service.ProjectMilestoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectMilestoneServiceImpl implements ProjectMilestoneService {

    private final ProjectRepository projectRepository;
    private final ProjectMilestoneRepository milestoneRepository;
    private final ProjectMilestoneMapper milestoneMapper;

    @Override
    @Transactional
    public ProjectMilestoneResponse createMilestone(UUID projectId, CreateMilestoneRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Creating milestone '{}' for project: {}", request.getName(), projectId);

        Project project = findProjectOrThrow(projectId, organizationId);

        ProjectMilestone milestone = new ProjectMilestone();
        milestone.setProject(project);
        milestone.setOrganizationId(organizationId);
        milestone.setName(request.getName().trim());
        milestone.setDescription(request.getDescription());
        milestone.setDueDate(request.getDueDate());
        milestone.setStatus(request.getStatus() != null ? request.getStatus() : MilestoneStatus.OPEN);

        ProjectMilestone saved = milestoneRepository.save(milestone);
        log.info("Milestone created with ID: {}", saved.getId());

        return milestoneMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMilestoneResponse> getMilestones(UUID projectId) {
        UUID organizationId = getRequiredOrganizationId();
        log.debug("Fetching milestones for project: {}", projectId);
        findProjectOrThrow(projectId, organizationId);

        return milestoneRepository.findByProjectId(projectId).stream()
                .map(milestoneMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProjectMilestoneResponse updateMilestone(UUID projectId, UUID milestoneId, UpdateMilestoneRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Updating milestone: {} for project: {}", milestoneId, projectId);
        findProjectOrThrow(projectId, organizationId);

        ProjectMilestone milestone = milestoneRepository.findByIdAndProjectId(milestoneId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with ID: " + milestoneId));

        if (request.getName() != null && !request.getName().isBlank()) {
            milestone.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            milestone.setDescription(request.getDescription());
        }
        if (request.getDueDate() != null) {
            milestone.setDueDate(request.getDueDate());
        }
        if (request.getStatus() != null) {
            milestone.setStatus(request.getStatus());
        }

        ProjectMilestone updated = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteMilestone(UUID projectId, UUID milestoneId) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Deleting milestone: {} for project: {}", milestoneId, projectId);
        findProjectOrThrow(projectId, organizationId);

        ProjectMilestone milestone = milestoneRepository.findByIdAndProjectId(milestoneId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with ID: " + milestoneId));

        milestoneRepository.delete(milestone);
    }

    private Project findProjectOrThrow(UUID projectId, UUID organizationId) {
        return projectRepository.findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
    }

    private UUID getRequiredOrganizationId() {
        UUID orgId = TenantContext.getOrganizationId();
        if (orgId == null) {
            throw new BadRequestException("Missing X-Organization-Id header for tenant isolation");
        }
        return orgId;
    }
}

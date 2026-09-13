package com.enterprise.platform.project.service.impl;

import com.enterprise.platform.events.ProjectEvent;
import com.enterprise.platform.events.ProjectEventType;
import com.enterprise.platform.project.constants.enums.MemberStatus;
import com.enterprise.platform.project.constants.enums.ProjectRole;
import com.enterprise.platform.project.constants.enums.ProjectStatus;
import com.enterprise.platform.project.constants.enums.ProjectVisibility;
import com.enterprise.platform.project.context.TenantContext;
import com.enterprise.platform.project.dto.request.CreateProjectRequest;
import com.enterprise.platform.project.dto.request.ProjectSearchCriteria;
import com.enterprise.platform.project.dto.request.UpdateProjectRequest;
import com.enterprise.platform.project.dto.response.PagedResponse;
import com.enterprise.platform.project.dto.response.ProjectResponse;
import com.enterprise.platform.project.entity.Project;
import com.enterprise.platform.project.entity.ProjectMember;
import com.enterprise.platform.project.exception.BadRequestException;
import com.enterprise.platform.project.exception.ConflictException;
import com.enterprise.platform.project.exception.ResourceNotFoundException;
import com.enterprise.platform.project.kafka.producer.ProjectEventProducer;
import com.enterprise.platform.project.mapper.ProjectMapper;
import com.enterprise.platform.project.repository.ProjectMemberRepository;
import com.enterprise.platform.project.repository.ProjectMilestoneRepository;
import com.enterprise.platform.project.repository.ProjectRepository;
import com.enterprise.platform.project.service.ProjectService;
import com.enterprise.platform.project.specification.ProjectSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMilestoneRepository projectMilestoneRepository;
    private final ProjectMapper projectMapper;
    private final ProjectEventProducer eventProducer;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        String normalizedKey = request.getProjectKey().trim().toUpperCase();
        log.info("Creating project '{}' ({}) in organization {}", request.getName(), normalizedKey, organizationId);

        if (projectRepository.existsByOrganizationIdAndProjectKey(organizationId, normalizedKey)) {
            throw new ConflictException("Project with key '" + normalizedKey + "' already exists in organization");
        }

        if (projectRepository.existsByOrganizationIdAndName(organizationId, request.getName().trim())) {
            throw new ConflictException("Project with name '" + request.getName() + "' already exists in organization");
        }

        Project project = new Project();
        project.setOrganizationId(organizationId);
        project.setName(request.getName().trim());
        project.setProjectKey(normalizedKey);
        project.setDescription(request.getDescription());
        project.setProjectType(request.getProjectType());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setVisibility(request.getVisibility() != null ? request.getVisibility() : ProjectVisibility.PUBLIC);
        project.setLeadUserId(request.getLeadUserId());
        project.setStartDate(request.getStartDate());
        project.setTargetEndDate(request.getTargetEndDate());
        project.setBudget(request.getBudget());
        project.setCurrency(request.getCurrency() != null ? request.getCurrency().trim().toUpperCase() : "USD");

        // Automatically assign lead as initial project member
        ProjectMember leadMember = new ProjectMember();
        leadMember.setProject(project);
        leadMember.setOrganizationId(organizationId);
        leadMember.setUserId(request.getLeadUserId());
        leadMember.setRole(ProjectRole.PROJECT_LEAD);
        leadMember.setStatus(MemberStatus.ACTIVE);
        leadMember.setJoinedAt(LocalDateTime.now());
        project.getMembers().add(leadMember);

        Project saved = projectRepository.save(project);
        log.info("Project '{}' created successfully with ID: {}", saved.getProjectKey(), saved.getId());

        publishEvent(
                ProjectEventType.PROJECT_CREATED,
                organizationId,
                saved.getId(),
                saved.getName(),
                saved.getProjectKey(),
                request.getLeadUserId(),
                ProjectRole.PROJECT_LEAD.name(),
                "Project created with lead user"
        );

        ProjectResponse response = projectMapper.toResponse(saved);
        response.setMemberCount(1L);
        response.setMilestoneCount(0L);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id) {
        UUID organizationId = getRequiredOrganizationId();
        log.debug("Fetching project ID: {} for organization: {}", id, organizationId);
        Project project = findProjectOrThrow(id, organizationId);
        return buildResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectByKey(String projectKey) {
        UUID organizationId = getRequiredOrganizationId();
        String normalizedKey = projectKey.trim().toUpperCase();
        log.debug("Fetching project key: {} for organization: {}", normalizedKey, organizationId);

        Project project = projectRepository.findByOrganizationIdAndProjectKey(organizationId, normalizedKey)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with key: " + normalizedKey));

        return buildResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Updating project ID: {} for organization: {}", id, organizationId);
        Project project = findProjectOrThrow(id, organizationId);

        if (request.getName() != null && !request.getName().trim().equalsIgnoreCase(project.getName())) {
            if (projectRepository.existsByOrganizationIdAndName(organizationId, request.getName().trim())) {
                throw new ConflictException("Project with name '" + request.getName() + "' already exists in organization");
            }
            project.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getProjectType() != null) {
            project.setProjectType(request.getProjectType());
        }
        if (request.getVisibility() != null) {
            project.setVisibility(request.getVisibility());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getLeadUserId() != null) {
            project.setLeadUserId(request.getLeadUserId());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getTargetEndDate() != null) {
            project.setTargetEndDate(request.getTargetEndDate());
        }
        if (request.getActualEndDate() != null) {
            project.setActualEndDate(request.getActualEndDate());
        }
        if (request.getBudget() != null) {
            project.setBudget(request.getBudget());
        }
        if (request.getCurrency() != null) {
            project.setCurrency(request.getCurrency().trim().toUpperCase());
        }

        Project updated = projectRepository.save(project);
        log.info("Project ID: {} updated successfully", updated.getId());

        publishEvent(
                ProjectEventType.PROJECT_UPDATED,
                organizationId,
                updated.getId(),
                updated.getName(),
                updated.getProjectKey(),
                null,
                null,
                "Project details updated"
        );

        return buildResponse(updated);
    }

    @Override
    @Transactional
    public ProjectResponse updateProjectStatus(UUID id, ProjectStatus status) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Updating status of project ID: {} to {}", id, status);
        Project project = findProjectOrThrow(id, organizationId);
        project.setStatus(status);

        Project updated = projectRepository.save(project);

        ProjectEventType eventType = status == ProjectStatus.ARCHIVED
                ? ProjectEventType.PROJECT_ARCHIVED
                : ProjectEventType.PROJECT_STATUS_CHANGED;

        publishEvent(
                eventType,
                organizationId,
                updated.getId(),
                updated.getName(),
                updated.getProjectKey(),
                null,
                null,
                "Project status updated to " + status
        );

        return buildResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProjectResponse> searchProjects(ProjectSearchCriteria criteria, Pageable pageable) {
        UUID organizationId = getRequiredOrganizationId();
        log.debug("Searching projects for organization: {} with pageable: {}", organizationId, pageable);

        Specification<Project> spec = ProjectSpecification.withCriteria(organizationId, criteria);
        Page<Project> page = projectRepository.findAll(spec, pageable);

        List<ProjectResponse> content = page.getContent().stream()
                .map(this::buildResponse)
                .toList();

        return PagedResponse.<ProjectResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public void deleteProject(UUID id) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Archiving project ID: {} for organization: {}", id, organizationId);
        Project project = findProjectOrThrow(id, organizationId);
        project.setStatus(ProjectStatus.ARCHIVED);
        projectRepository.save(project);

        publishEvent(
                ProjectEventType.PROJECT_ARCHIVED,
                organizationId,
                project.getId(),
                project.getName(),
                project.getProjectKey(),
                null,
                null,
                "Project archived"
        );
    }

    private Project findProjectOrThrow(UUID id, UUID organizationId) {
        return projectRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));
    }

    private ProjectResponse buildResponse(Project project) {
        ProjectResponse response = projectMapper.toResponse(project);
        response.setMemberCount(projectMemberRepository.countByProjectId(project.getId()));
        response.setMilestoneCount(projectMilestoneRepository.findByProjectId(project.getId()).size());
        return response;
    }

    private UUID getRequiredOrganizationId() {
        UUID orgId = TenantContext.getOrganizationId();
        if (orgId == null) {
            throw new BadRequestException("Missing X-Organization-Id header for tenant isolation");
        }
        return orgId;
    }

    private void publishEvent(
            ProjectEventType eventType,
            UUID organizationId,
            UUID projectId,
            String projectName,
            String projectKey,
            UUID userId,
            String memberRole,
            String details
    ) {
        ProjectEvent event = ProjectEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .organizationId(organizationId)
                .projectId(projectId)
                .projectName(projectName)
                .projectKey(projectKey)
                .userId(userId)
                .memberRole(memberRole)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
        eventProducer.publish(event);
    }
}

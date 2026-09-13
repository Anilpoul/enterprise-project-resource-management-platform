package com.enterprise.platform.project.service.impl;

import com.enterprise.platform.events.ProjectEvent;
import com.enterprise.platform.events.ProjectEventType;
import com.enterprise.platform.project.constants.enums.MemberStatus;
import com.enterprise.platform.project.context.TenantContext;
import com.enterprise.platform.project.dto.request.AddProjectMemberRequest;
import com.enterprise.platform.project.dto.request.UpdateProjectMemberRoleRequest;
import com.enterprise.platform.project.dto.response.PagedResponse;
import com.enterprise.platform.project.dto.response.ProjectMemberResponse;
import com.enterprise.platform.project.entity.Project;
import com.enterprise.platform.project.entity.ProjectMember;
import com.enterprise.platform.project.exception.BadRequestException;
import com.enterprise.platform.project.exception.ConflictException;
import com.enterprise.platform.project.exception.ResourceNotFoundException;
import com.enterprise.platform.project.kafka.producer.ProjectEventProducer;
import com.enterprise.platform.project.mapper.ProjectMemberMapper;
import com.enterprise.platform.project.repository.ProjectMemberRepository;
import com.enterprise.platform.project.repository.ProjectRepository;
import com.enterprise.platform.project.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectMemberMapper memberMapper;
    private final ProjectEventProducer eventProducer;

    @Override
    @Transactional
    public ProjectMemberResponse addMember(UUID projectId, AddProjectMemberRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Adding user {} to project {} in organization {}", request.getUserId(), projectId, organizationId);

        Project project = findProjectOrThrow(projectId, organizationId);

        if (memberRepository.existsByProjectIdAndUserId(projectId, request.getUserId())) {
            throw new ConflictException("User is already a member of this project");
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setOrganizationId(organizationId);
        member.setUserId(request.getUserId());
        member.setRole(request.getRole());
        member.setStatus(MemberStatus.ACTIVE);
        member.setJoinedAt(LocalDateTime.now());

        ProjectMember saved = memberRepository.save(member);
        log.info("Project member added with ID: {}, role: {}", saved.getId(), saved.getRole());

        publishEvent(
                ProjectEventType.PROJECT_MEMBER_ADDED,
                organizationId,
                project.getId(),
                project.getName(),
                project.getProjectKey(),
                saved.getUserId(),
                saved.getRole().name(),
                "User added to project with role " + saved.getRole()
        );

        return memberMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProjectMemberResponse> getMembers(UUID projectId, Pageable pageable) {
        UUID organizationId = getRequiredOrganizationId();
        log.debug("Fetching members for project: {} in organization: {}", projectId, organizationId);
        findProjectOrThrow(projectId, organizationId);

        Page<ProjectMember> page = memberRepository.findByProjectId(projectId, pageable);
        List<ProjectMemberResponse> content = page.getContent().stream()
                .map(memberMapper::toResponse)
                .toList();

        return PagedResponse.<ProjectMemberResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectMemberResponse getMember(UUID projectId, UUID userId) {
        UUID organizationId = getRequiredOrganizationId();
        log.debug("Fetching member {} for project: {}", userId, projectId);
        findProjectOrThrow(projectId, organizationId);

        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in project"));

        return memberMapper.toResponse(member);
    }

    @Override
    @Transactional
    public ProjectMemberResponse updateMemberRole(UUID projectId, UUID userId, UpdateProjectMemberRoleRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Updating role for member {} in project {} to {}", userId, projectId, request.getRole());

        Project project = findProjectOrThrow(projectId, organizationId);
        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in project"));

        member.setRole(request.getRole());
        ProjectMember updated = memberRepository.save(member);

        publishEvent(
                ProjectEventType.PROJECT_MEMBER_ROLE_UPDATED,
                organizationId,
                project.getId(),
                project.getName(),
                project.getProjectKey(),
                userId,
                request.getRole().name(),
                "Member role updated to " + request.getRole()
        );

        return memberMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void removeMember(UUID projectId, UUID userId) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Removing member {} from project {}", userId, projectId);

        Project project = findProjectOrThrow(projectId, organizationId);
        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in project"));

        memberRepository.delete(member);

        publishEvent(
                ProjectEventType.PROJECT_MEMBER_REMOVED,
                organizationId,
                project.getId(),
                project.getName(),
                project.getProjectKey(),
                userId,
                member.getRole().name(),
                "Member removed from project"
        );
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

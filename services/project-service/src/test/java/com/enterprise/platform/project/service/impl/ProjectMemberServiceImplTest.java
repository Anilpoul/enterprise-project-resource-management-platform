package com.enterprise.platform.project.service.impl;

import com.enterprise.platform.events.ProjectEvent;
import com.enterprise.platform.events.ProjectEventType;
import com.enterprise.platform.project.constants.enums.MemberStatus;
import com.enterprise.platform.project.constants.enums.ProjectRole;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository memberRepository;

    @Mock
    private ProjectMemberMapper memberMapper;

    @Mock
    private ProjectEventProducer eventProducer;

    @InjectMocks
    private ProjectMemberServiceImpl memberService;

    private UUID organizationId;
    private UUID projectId;
    private UUID userId;
    private Project project;
    private ProjectMember member;
    private ProjectMemberResponse memberResponse;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();

        TenantContext.setOrganizationId(organizationId);

        project = new Project();
        project.setId(projectId);
        project.setOrganizationId(organizationId);
        project.setName("Payments Service");
        project.setProjectKey("PAY");

        member = new ProjectMember();
        member.setId(UUID.randomUUID());
        member.setProject(project);
        member.setOrganizationId(organizationId);
        member.setUserId(userId);
        member.setRole(ProjectRole.DEVELOPER);
        member.setStatus(MemberStatus.ACTIVE);
        member.setJoinedAt(LocalDateTime.now());

        memberResponse = ProjectMemberResponse.builder()
                .id(member.getId())
                .projectId(projectId)
                .organizationId(organizationId)
                .userId(userId)
                .role(ProjectRole.DEVELOPER)
                .status(MemberStatus.ACTIVE)
                .joinedAt(member.getJoinedAt())
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should add member successfully and publish event")
    void testAddMember_Success() {
        AddProjectMemberRequest request = new AddProjectMemberRequest();
        request.setUserId(userId);
        request.setRole(ProjectRole.DEVELOPER);

        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserId(projectId, userId)).thenReturn(false);
        when(memberRepository.save(any(ProjectMember.class))).thenReturn(member);
        when(memberMapper.toResponse(member)).thenReturn(memberResponse);

        ProjectMemberResponse response = memberService.addMember(projectId, request);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getRole()).isEqualTo(ProjectRole.DEVELOPER);

        verify(memberRepository).save(any(ProjectMember.class));
        ArgumentCaptor<ProjectEvent> eventCaptor = ArgumentCaptor.forClass(ProjectEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        ProjectEvent published = eventCaptor.getValue();
        assertThat(published.getEventType()).isEqualTo(ProjectEventType.PROJECT_MEMBER_ADDED);
        assertThat(published.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should throw ConflictException when member already exists")
    void testAddMember_DuplicateMember() {
        AddProjectMemberRequest request = new AddProjectMemberRequest();
        request.setUserId(userId);
        request.setRole(ProjectRole.DEVELOPER);

        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(memberRepository.existsByProjectIdAndUserId(projectId, userId)).thenReturn(true);

        assertThatThrownBy(() -> memberService.addMember(projectId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already a member");

        verify(memberRepository, never()).save(any());
        verify(eventProducer, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project not found during addMember")
    void testAddMember_ProjectNotFound() {
        AddProjectMemberRequest request = new AddProjectMemberRequest();
        request.setUserId(userId);
        request.setRole(ProjectRole.DEVELOPER);

        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.addMember(projectId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");
    }

    @Test
    @DisplayName("Should throw BadRequestException when tenant organizationId is missing")
    void testAddMember_MissingTenantContext() {
        TenantContext.clear();
        AddProjectMemberRequest request = new AddProjectMemberRequest();
        request.setUserId(userId);
        request.setRole(ProjectRole.DEVELOPER);

        assertThatThrownBy(() -> memberService.addMember(projectId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Missing X-Organization-Id");
    }

    @Test
    @DisplayName("Should get paginated members for project")
    void testGetMembers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProjectMember> page = new PageImpl<>(List.of(member), pageable, 1);

        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(memberRepository.findByProjectId(projectId, pageable)).thenReturn(page);
        when(memberMapper.toResponse(member)).thenReturn(memberResponse);

        PagedResponse<ProjectMemberResponse> result = memberService.getMembers(projectId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should get specific member by projectId and userId")
    void testGetMember_Success() {
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(memberRepository.findByProjectIdAndUserId(projectId, userId)).thenReturn(Optional.of(member));
        when(memberMapper.toResponse(member)).thenReturn(memberResponse);

        ProjectMemberResponse result = memberService.getMember(projectId, userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when member not found in project")
    void testGetMember_NotFound() {
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(memberRepository.findByProjectIdAndUserId(projectId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMember(projectId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Member not found");
    }

    @Test
    @DisplayName("Should update member role successfully and publish event")
    void testUpdateMemberRole_Success() {
        UpdateProjectMemberRoleRequest request = new UpdateProjectMemberRoleRequest();
        request.setRole(ProjectRole.PRODUCT_OWNER);

        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(memberRepository.findByProjectIdAndUserId(projectId, userId)).thenReturn(Optional.of(member));
        when(memberRepository.save(member)).thenReturn(member);

        ProjectMemberResponse updatedResponse = ProjectMemberResponse.builder()
                .id(member.getId())
                .projectId(projectId)
                .organizationId(organizationId)
                .userId(userId)
                .role(ProjectRole.PRODUCT_OWNER)
                .status(MemberStatus.ACTIVE)
                .build();
        when(memberMapper.toResponse(member)).thenReturn(updatedResponse);

        ProjectMemberResponse result = memberService.updateMemberRole(projectId, userId, request);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(ProjectRole.PRODUCT_OWNER);

        verify(memberRepository).save(member);
        ArgumentCaptor<ProjectEvent> eventCaptor = ArgumentCaptor.forClass(ProjectEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(ProjectEventType.PROJECT_MEMBER_ROLE_UPDATED);
    }

    @Test
    @DisplayName("Should remove member successfully and publish event")
    void testRemoveMember_Success() {
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(memberRepository.findByProjectIdAndUserId(projectId, userId)).thenReturn(Optional.of(member));

        memberService.removeMember(projectId, userId);

        verify(memberRepository).delete(member);
        ArgumentCaptor<ProjectEvent> eventCaptor = ArgumentCaptor.forClass(ProjectEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(ProjectEventType.PROJECT_MEMBER_REMOVED);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when removing non-existent member")
    void testRemoveMember_NotFound() {
        when(projectRepository.findByIdAndOrganizationId(projectId, organizationId)).thenReturn(Optional.of(project));
        when(memberRepository.findByProjectIdAndUserId(projectId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.removeMember(projectId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Member not found");

        verify(memberRepository, never()).delete(any());
    }
}

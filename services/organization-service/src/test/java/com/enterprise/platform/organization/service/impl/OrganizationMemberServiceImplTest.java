package com.enterprise.platform.organization.service.impl;

import com.enterprise.platform.events.OrganizationEvent;
import com.enterprise.platform.events.OrganizationEventType;
import com.enterprise.platform.organization.constants.enums.MemberStatus;
import com.enterprise.platform.organization.constants.enums.OrganizationRole;
import com.enterprise.platform.organization.dto.request.AddMemberRequest;
import com.enterprise.platform.organization.dto.request.UpdateMemberRoleRequest;
import com.enterprise.platform.organization.dto.response.OrganizationMemberResponse;
import com.enterprise.platform.organization.dto.response.PagedResponse;
import com.enterprise.platform.organization.entity.Organization;
import com.enterprise.platform.organization.entity.OrganizationMember;
import com.enterprise.platform.organization.entity.OrganizationSettings;
import com.enterprise.platform.organization.exception.BadRequestException;
import com.enterprise.platform.organization.exception.ConflictException;
import com.enterprise.platform.organization.exception.ResourceNotFoundException;
import com.enterprise.platform.organization.kafka.producer.OrganizationEventProducer;
import com.enterprise.platform.organization.mapper.OrganizationMemberMapper;
import com.enterprise.platform.organization.repository.OrganizationMemberRepository;
import com.enterprise.platform.organization.repository.OrganizationRepository;
import com.enterprise.platform.organization.repository.OrganizationSettingsRepository;
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
class OrganizationMemberServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository memberRepository;

    @Mock
    private OrganizationSettingsRepository settingsRepository;

    @Mock
    private OrganizationMemberMapper memberMapper;

    @Mock
    private OrganizationEventProducer eventProducer;

    @InjectMocks
    private OrganizationMemberServiceImpl memberService;

    private UUID orgId;
    private UUID userId;
    private Organization organization;
    private OrganizationMember member;
    private OrganizationMemberResponse memberResponse;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        userId = UUID.randomUUID();

        organization = new Organization();
        organization.setId(orgId);
        organization.setName("Acme Corp");
        organization.setSlug("acme-corp");

        member = new OrganizationMember();
        member.setId(UUID.randomUUID());
        member.setOrganization(organization);
        member.setUserId(userId);
        member.setRole(OrganizationRole.ORG_MEMBER);
        member.setStatus(MemberStatus.ACTIVE);
        member.setJoinedAt(LocalDateTime.now());

        memberResponse = OrganizationMemberResponse.builder()
                .id(member.getId())
                .organizationId(orgId)
                .userId(userId)
                .role(OrganizationRole.ORG_MEMBER)
                .status(MemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Add member successfully and publish member added event")
    void testAddMember_Success() {
        AddMemberRequest request = AddMemberRequest.builder()
                .userId(userId)
                .role(OrganizationRole.ORG_VIEWER)
                .build();

        OrganizationSettings settings = new OrganizationSettings();
        settings.setMaxUsers(10);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(memberRepository.existsByOrganizationIdAndUserId(orgId, userId)).thenReturn(false);
        when(settingsRepository.findByOrganizationId(orgId)).thenReturn(Optional.of(settings));
        when(memberRepository.countByOrganizationId(orgId)).thenReturn(2L);
        when(memberRepository.save(any(OrganizationMember.class))).thenReturn(member);
        when(memberMapper.toResponse(any(OrganizationMember.class))).thenReturn(memberResponse);

        OrganizationMemberResponse result = memberService.addMember(orgId, request);

        assertThat(result).isNotNull();
        verify(memberRepository).save(any(OrganizationMember.class));

        ArgumentCaptor<OrganizationEvent> captor = ArgumentCaptor.forClass(OrganizationEvent.class);
        verify(eventProducer).publish(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(OrganizationEventType.ORGANIZATION_MEMBER_ADDED);
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Add member throws ConflictException when user already a member")
    void testAddMember_AlreadyMember() {
        AddMemberRequest request = AddMemberRequest.builder()
                .userId(userId)
                .role(OrganizationRole.ORG_MEMBER)
                .build();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(memberRepository.existsByOrganizationIdAndUserId(orgId, userId)).thenReturn(true);

        assertThatThrownBy(() -> memberService.addMember(orgId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already a member");

        verify(memberRepository, never()).save(any());
        verify(eventProducer, never()).publish(any());
    }

    @Test
    @DisplayName("Add member throws BadRequestException when organization max users reached")
    void testAddMember_MaxUsersExceeded() {
        AddMemberRequest request = AddMemberRequest.builder()
                .userId(userId)
                .role(OrganizationRole.ORG_MEMBER)
                .build();

        OrganizationSettings settings = new OrganizationSettings();
        settings.setMaxUsers(5);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(memberRepository.existsByOrganizationIdAndUserId(orgId, userId)).thenReturn(false);
        when(settingsRepository.findByOrganizationId(orgId)).thenReturn(Optional.of(settings));
        when(memberRepository.countByOrganizationId(orgId)).thenReturn(5L);

        assertThatThrownBy(() -> memberService.addMember(orgId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("maximum user limit");

        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get members with pagination successfully")
    void testGetMembers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrganizationMember> page = new PageImpl<>(List.of(member), pageable, 1);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(memberRepository.findByOrganizationId(orgId, pageable)).thenReturn(page);
        when(memberMapper.toResponse(member)).thenReturn(memberResponse);

        PagedResponse<OrganizationMemberResponse> result = memberService.getMembers(orgId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Update member role successfully and publish role changed event")
    void testUpdateMemberRole_Success() {
        UpdateMemberRoleRequest request = UpdateMemberRoleRequest.builder()
                .role(OrganizationRole.ORG_MANAGER)
                .build();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(memberRepository.findByOrganizationIdAndUserId(orgId, userId)).thenReturn(Optional.of(member));
        when(memberRepository.save(member)).thenReturn(member);
        when(memberMapper.toResponse(member)).thenReturn(memberResponse);

        OrganizationMemberResponse result = memberService.updateMemberRole(orgId, userId, request);

        assertThat(result).isNotNull();
        assertThat(member.getRole()).isEqualTo(OrganizationRole.ORG_MANAGER);

        ArgumentCaptor<OrganizationEvent> captor = ArgumentCaptor.forClass(OrganizationEvent.class);
        verify(eventProducer).publish(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(OrganizationEventType.ORGANIZATION_MEMBER_ROLE_CHANGED);
        assertThat(captor.getValue().getMemberRole()).isEqualTo(OrganizationRole.ORG_MANAGER.name());
    }

    @Test
    @DisplayName("Remove member successfully and publish member removed event")
    void testRemoveMember_Success() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(memberRepository.findByOrganizationIdAndUserId(orgId, userId)).thenReturn(Optional.of(member));

        memberService.removeMember(orgId, userId);

        verify(memberRepository).delete(member);

        ArgumentCaptor<OrganizationEvent> captor = ArgumentCaptor.forClass(OrganizationEvent.class);
        verify(eventProducer).publish(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(OrganizationEventType.ORGANIZATION_MEMBER_REMOVED);
    }

    @Test
    @DisplayName("Get member throws ResourceNotFoundException when member not found")
    void testGetMember_NotFound() {
        when(memberRepository.findByOrganizationIdAndUserId(orgId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMember(orgId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }
}

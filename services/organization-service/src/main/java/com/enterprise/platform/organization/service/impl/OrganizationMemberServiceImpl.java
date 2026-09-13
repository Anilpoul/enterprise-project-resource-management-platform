package com.enterprise.platform.organization.service.impl;

import com.enterprise.platform.events.OrganizationEvent;
import com.enterprise.platform.events.OrganizationEventType;
import com.enterprise.platform.organization.constants.enums.MemberStatus;
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
import com.enterprise.platform.organization.service.OrganizationMemberService;
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
public class OrganizationMemberServiceImpl implements OrganizationMemberService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final OrganizationSettingsRepository settingsRepository;
    private final OrganizationMemberMapper memberMapper;
    private final OrganizationEventProducer eventProducer;

    @Override
    @Transactional
    public OrganizationMemberResponse addMember(UUID organizationId, AddMemberRequest request) {
        log.info("Adding user {} to organization {}", request.getUserId(), organizationId);
        Organization organization = findOrganizationOrThrow(organizationId);

        if (memberRepository.existsByOrganizationIdAndUserId(organizationId, request.getUserId())) {
            throw new ConflictException("User is already a member of this organization");
        }

        settingsRepository.findByOrganizationId(organizationId).ifPresent(settings -> {
            long currentCount = memberRepository.countByOrganizationId(organizationId);
            if (currentCount >= settings.getMaxUsers()) {
                throw new BadRequestException("Organization has reached its maximum user limit of " + settings.getMaxUsers());
            }
        });

        OrganizationMember member = new OrganizationMember();
        member.setOrganization(organization);
        member.setUserId(request.getUserId());
        member.setRole(request.getRole());
        member.setStatus(MemberStatus.ACTIVE);
        member.setJoinedAt(LocalDateTime.now());

        OrganizationMember saved = memberRepository.save(member);
        log.info("Member {} added successfully with role {}", saved.getId(), saved.getRole());

        publishEvent(
                OrganizationEventType.ORGANIZATION_MEMBER_ADDED,
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                saved.getUserId(),
                saved.getRole().name(),
                "User added to organization as " + saved.getRole()
        );

        return memberMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrganizationMemberResponse> getMembers(UUID organizationId, Pageable pageable) {
        log.debug("Fetching members for organization {}", organizationId);
        findOrganizationOrThrow(organizationId);

        Page<OrganizationMember> page = memberRepository.findByOrganizationId(organizationId, pageable);
        List<OrganizationMemberResponse> content = page.getContent().stream()
                .map(memberMapper::toResponse)
                .toList();

        return PagedResponse.<OrganizationMemberResponse>builder()
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
    public OrganizationMemberResponse getMember(UUID organizationId, UUID userId) {
        log.debug("Fetching member {} for organization {}", userId, organizationId);
        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in organization"));
        return memberMapper.toResponse(member);
    }

    @Override
    @Transactional
    public OrganizationMemberResponse updateMemberRole(UUID organizationId, UUID userId, UpdateMemberRoleRequest request) {
        log.info("Updating role for member {} in organization {} to {}", userId, organizationId, request.getRole());
        Organization organization = findOrganizationOrThrow(organizationId);

        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in organization"));

        member.setRole(request.getRole());
        OrganizationMember updated = memberRepository.save(member);

        publishEvent(
                OrganizationEventType.ORGANIZATION_MEMBER_ROLE_CHANGED,
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                userId,
                request.getRole().name(),
                "Member role updated to " + request.getRole()
        );

        return memberMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void removeMember(UUID organizationId, UUID userId) {
        log.info("Removing member {} from organization {}", userId, organizationId);
        Organization organization = findOrganizationOrThrow(organizationId);

        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in organization"));

        memberRepository.delete(member);

        publishEvent(
                OrganizationEventType.ORGANIZATION_MEMBER_REMOVED,
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                userId,
                member.getRole().name(),
                "Member removed from organization"
        );
    }

    private Organization findOrganizationOrThrow(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + id));
    }

    private void publishEvent(
            OrganizationEventType eventType,
            UUID orgId,
            String orgName,
            String orgSlug,
            UUID userId,
            String memberRole,
            String details
    ) {
        OrganizationEvent event = OrganizationEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .organizationId(orgId)
                .organizationName(orgName)
                .organizationSlug(orgSlug)
                .userId(userId)
                .memberRole(memberRole)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
        eventProducer.publish(event);
    }
}

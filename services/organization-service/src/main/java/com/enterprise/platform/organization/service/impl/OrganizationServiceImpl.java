package com.enterprise.platform.organization.service.impl;

import com.enterprise.platform.events.OrganizationEvent;
import com.enterprise.platform.events.OrganizationEventType;
import com.enterprise.platform.organization.constants.enums.MemberStatus;
import com.enterprise.platform.organization.constants.enums.OrganizationRole;
import com.enterprise.platform.organization.constants.enums.OrganizationStatus;
import com.enterprise.platform.organization.dto.request.CreateOrganizationRequest;
import com.enterprise.platform.organization.dto.request.UpdateOrganizationRequest;
import com.enterprise.platform.organization.dto.response.OrganizationResponse;
import com.enterprise.platform.organization.dto.response.PagedResponse;
import com.enterprise.platform.organization.entity.Organization;
import com.enterprise.platform.organization.entity.OrganizationMember;
import com.enterprise.platform.organization.entity.OrganizationSettings;
import com.enterprise.platform.organization.exception.ConflictException;
import com.enterprise.platform.organization.exception.ResourceNotFoundException;
import com.enterprise.platform.organization.kafka.producer.OrganizationEventProducer;
import com.enterprise.platform.organization.mapper.OrganizationMapper;
import com.enterprise.platform.organization.repository.OrganizationMemberRepository;
import com.enterprise.platform.organization.repository.OrganizationRepository;
import com.enterprise.platform.organization.repository.OrganizationSettingsRepository;
import com.enterprise.platform.organization.service.OrganizationService;
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
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationSettingsRepository organizationSettingsRepository;
    private final OrganizationMapper organizationMapper;
    private final OrganizationEventProducer eventProducer;

    @Override
    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        log.info("Creating organization with name: {}, slug: {}", request.getName(), request.getSlug());

        if (organizationRepository.existsByName(request.getName())) {
            throw new ConflictException("Organization with name '" + request.getName() + "' already exists");
        }

        if (organizationRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException("Organization with slug '" + request.getSlug() + "' already exists");
        }

        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setSlug(request.getSlug());
        organization.setDescription(request.getDescription());
        organization.setLogoUrl(request.getLogoUrl());
        organization.setStatus(OrganizationStatus.ACTIVE);

        // Default Settings
        OrganizationSettings settings = new OrganizationSettings();
        settings.setOrganization(organization);
        organization.setSettings(settings);

        // Admin Member
        OrganizationMember adminMember = new OrganizationMember();
        adminMember.setOrganization(organization);
        adminMember.setUserId(request.getAdminUserId());
        adminMember.setRole(OrganizationRole.ORG_ADMIN);
        adminMember.setStatus(MemberStatus.ACTIVE);
        adminMember.setJoinedAt(LocalDateTime.now());
        organization.getMembers().add(adminMember);

        Organization saved = organizationRepository.save(organization);
        log.info("Organization created successfully with ID: {}", saved.getId());

        // Publish event
        publishEvent(
                OrganizationEventType.ORGANIZATION_CREATED,
                saved.getId(),
                saved.getName(),
                saved.getSlug(),
                request.getAdminUserId(),
                OrganizationRole.ORG_ADMIN.name(),
                "Organization created with initial admin user"
        );

        OrganizationResponse response = organizationMapper.toResponse(saved);
        response.setMemberCount(1L);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(UUID id) {
        log.debug("Fetching organization with ID: {}", id);
        Organization organization = findOrganizationOrThrow(id);
        OrganizationResponse response = organizationMapper.toResponse(organization);
        response.setMemberCount(organizationMemberRepository.countByOrganizationId(id));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationBySlug(String slug) {
        log.debug("Fetching organization with slug: {}", slug);
        Organization organization = organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with slug: " + slug));
        OrganizationResponse response = organizationMapper.toResponse(organization);
        response.setMemberCount(organizationMemberRepository.countByOrganizationId(organization.getId()));
        return response;
    }

    @Override
    @Transactional
    public OrganizationResponse updateOrganization(UUID id, UpdateOrganizationRequest request) {
        log.info("Updating organization with ID: {}", id);
        Organization organization = findOrganizationOrThrow(id);

        if (request.getName() != null && !request.getName().equals(organization.getName())) {
            if (organizationRepository.existsByName(request.getName())) {
                throw new ConflictException("Organization with name '" + request.getName() + "' already exists");
            }
            organization.setName(request.getName());
        }

        if (request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }

        if (request.getLogoUrl() != null) {
            organization.setLogoUrl(request.getLogoUrl());
        }

        if (request.getStatus() != null) {
            organization.setStatus(request.getStatus());
        }

        Organization updated = organizationRepository.save(organization);
        log.info("Organization updated successfully with ID: {}", updated.getId());

        publishEvent(
                OrganizationEventType.ORGANIZATION_UPDATED,
                updated.getId(),
                updated.getName(),
                updated.getSlug(),
                null,
                null,
                "Organization details updated"
        );

        OrganizationResponse response = organizationMapper.toResponse(updated);
        response.setMemberCount(organizationMemberRepository.countByOrganizationId(updated.getId()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrganizationResponse> searchOrganizations(String query, Pageable pageable) {
        log.debug("Searching organizations with query: {}, pageable: {}", query, pageable);
        Page<Organization> page;

        if (query != null && !query.isBlank()) {
            page = organizationRepository.findByNameContainingIgnoreCaseOrSlugContainingIgnoreCase(
                    query.trim(),
                    query.trim(),
                    pageable
            );
        } else {
            page = organizationRepository.findAll(pageable);
        }

        List<OrganizationResponse> content = page.getContent().stream()
                .map(org -> {
                    OrganizationResponse res = organizationMapper.toResponse(org);
                    res.setMemberCount(organizationMemberRepository.countByOrganizationId(org.getId()));
                    return res;
                })
                .toList();

        return PagedResponse.<OrganizationResponse>builder()
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
    public OrganizationResponse updateStatus(UUID id, OrganizationStatus status) {
        log.info("Updating status of organization ID: {} to {}", id, status);
        Organization organization = findOrganizationOrThrow(id);
        organization.setStatus(status);

        Organization updated = organizationRepository.save(organization);

        OrganizationEventType eventType = switch (status) {
            case SUSPENDED -> OrganizationEventType.ORGANIZATION_SUSPENDED;
            case ARCHIVED -> OrganizationEventType.ORGANIZATION_DELETED;
            default -> OrganizationEventType.ORGANIZATION_UPDATED;
        };

        publishEvent(
                eventType,
                updated.getId(),
                updated.getName(),
                updated.getSlug(),
                null,
                null,
                "Organization status updated to " + status
        );

        OrganizationResponse response = organizationMapper.toResponse(updated);
        response.setMemberCount(organizationMemberRepository.countByOrganizationId(updated.getId()));
        return response;
    }

    @Override
    @Transactional
    public void deleteOrganization(UUID id) {
        log.info("Deleting organization ID: {}", id);
        Organization organization = findOrganizationOrThrow(id);
        organization.setStatus(OrganizationStatus.ARCHIVED);
        organizationRepository.save(organization);

        publishEvent(
                OrganizationEventType.ORGANIZATION_DELETED,
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                null,
                null,
                "Organization archived"
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

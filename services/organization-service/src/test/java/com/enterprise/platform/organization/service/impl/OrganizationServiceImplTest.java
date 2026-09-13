package com.enterprise.platform.organization.service.impl;

import com.enterprise.platform.events.OrganizationEvent;
import com.enterprise.platform.events.OrganizationEventType;
import com.enterprise.platform.organization.constants.enums.OrganizationRole;
import com.enterprise.platform.organization.constants.enums.OrganizationStatus;
import com.enterprise.platform.organization.dto.request.CreateOrganizationRequest;
import com.enterprise.platform.organization.dto.request.UpdateOrganizationRequest;
import com.enterprise.platform.organization.dto.response.OrganizationResponse;
import com.enterprise.platform.organization.dto.response.PagedResponse;
import com.enterprise.platform.organization.entity.Organization;
import com.enterprise.platform.organization.exception.ConflictException;
import com.enterprise.platform.organization.exception.ResourceNotFoundException;
import com.enterprise.platform.organization.kafka.producer.OrganizationEventProducer;
import com.enterprise.platform.organization.mapper.OrganizationMapper;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private OrganizationSettingsRepository organizationSettingsRepository;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private OrganizationEventProducer eventProducer;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    private UUID orgId;
    private UUID adminUserId;
    private Organization organization;
    private OrganizationResponse organizationResponse;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();

        organization = new Organization();
        organization.setId(orgId);
        organization.setName("Acme Corp");
        organization.setSlug("acme-corp");
        organization.setDescription("Acme Corporation");
        organization.setStatus(OrganizationStatus.ACTIVE);

        organizationResponse = OrganizationResponse.builder()
                .id(orgId)
                .name("Acme Corp")
                .slug("acme-corp")
                .description("Acme Corporation")
                .status(OrganizationStatus.ACTIVE)
                .memberCount(1L)
                .build();
    }

    @Test
    @DisplayName("Create organization successfully with default settings, admin member, and published event")
    void testCreateOrganization_Success() {
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("Acme Corp")
                .slug("acme-corp")
                .description("Acme Corporation")
                .adminUserId(adminUserId)
                .build();

        when(organizationRepository.existsByName("Acme Corp")).thenReturn(false);
        when(organizationRepository.existsBySlug("acme-corp")).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
            Organization org = invocation.getArgument(0);
            org.setId(orgId);
            return org;
        });
        when(organizationMapper.toResponse(any(Organization.class))).thenReturn(organizationResponse);

        OrganizationResponse result = organizationService.createOrganization(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Acme Corp");
        assertThat(result.getSlug()).isEqualTo("acme-corp");

        verify(organizationRepository).save(any(Organization.class));
        ArgumentCaptor<OrganizationEvent> eventCaptor = ArgumentCaptor.forClass(OrganizationEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());

        OrganizationEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getEventType()).isEqualTo(OrganizationEventType.ORGANIZATION_CREATED);
        assertThat(publishedEvent.getOrganizationName()).isEqualTo("Acme Corp");
        assertThat(publishedEvent.getUserId()).isEqualTo(adminUserId);
        assertThat(publishedEvent.getMemberRole()).isEqualTo(OrganizationRole.ORG_ADMIN.name());
    }

    @Test
    @DisplayName("Create organization throws ConflictException when name already exists")
    void testCreateOrganization_NameConflict() {
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("Acme Corp")
                .slug("acme-corp")
                .adminUserId(adminUserId)
                .build();

        when(organizationRepository.existsByName("Acme Corp")).thenReturn(true);

        assertThatThrownBy(() -> organizationService.createOrganization(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(organizationRepository, never()).save(any());
        verify(eventProducer, never()).publish(any());
    }

    @Test
    @DisplayName("Create organization throws ConflictException when slug already exists")
    void testCreateOrganization_SlugConflict() {
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("Acme Corp")
                .slug("acme-corp")
                .adminUserId(adminUserId)
                .build();

        when(organizationRepository.existsByName("Acme Corp")).thenReturn(false);
        when(organizationRepository.existsBySlug("acme-corp")).thenReturn(true);

        assertThatThrownBy(() -> organizationService.createOrganization(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(organizationRepository, never()).save(any());
        verify(eventProducer, never()).publish(any());
    }

    @Test
    @DisplayName("Get organization by ID successfully")
    void testGetOrganizationById_Success() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(organizationMapper.toResponse(organization)).thenReturn(organizationResponse);
        when(organizationMemberRepository.countByOrganizationId(orgId)).thenReturn(5L);

        OrganizationResponse result = organizationService.getOrganizationById(orgId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orgId);
        assertThat(result.getMemberCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Get organization by ID throws ResourceNotFoundException when not found")
    void testGetOrganizationById_NotFound() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.getOrganizationById(orgId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Get organization by slug successfully")
    void testGetOrganizationBySlug_Success() {
        when(organizationRepository.findBySlug("acme-corp")).thenReturn(Optional.of(organization));
        when(organizationMapper.toResponse(organization)).thenReturn(organizationResponse);
        when(organizationMemberRepository.countByOrganizationId(orgId)).thenReturn(1L);

        OrganizationResponse result = organizationService.getOrganizationBySlug("acme-corp");

        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo("acme-corp");
    }

    @Test
    @DisplayName("Update organization details successfully")
    void testUpdateOrganization_Success() {
        UpdateOrganizationRequest request = UpdateOrganizationRequest.builder()
                .name("Acme Worldwide")
                .description("New Description")
                .build();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(organizationRepository.existsByName("Acme Worldwide")).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenReturn(organization);
        when(organizationMapper.toResponse(any(Organization.class))).thenReturn(organizationResponse);
        when(organizationMemberRepository.countByOrganizationId(orgId)).thenReturn(2L);

        OrganizationResponse result = organizationService.updateOrganization(orgId, request);

        assertThat(result).isNotNull();
        verify(organizationRepository).save(organization);
        verify(eventProducer).publish(any(OrganizationEvent.class));
    }

    @Test
    @DisplayName("Search organizations with query string")
    void testSearchOrganizations_WithQuery() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Organization> page = new PageImpl<>(List.of(organization), pageable, 1);

        when(organizationRepository.findByNameContainingIgnoreCaseOrSlugContainingIgnoreCase("acme", "acme", pageable))
                .thenReturn(page);
        when(organizationMapper.toResponse(organization)).thenReturn(organizationResponse);
        when(organizationMemberRepository.countByOrganizationId(orgId)).thenReturn(1L);

        PagedResponse<OrganizationResponse> result = organizationService.searchOrganizations("acme", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Update status to SUSPENDED publishes correct event")
    void testUpdateStatus_Suspended() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(organizationRepository.save(any(Organization.class))).thenReturn(organization);
        when(organizationMapper.toResponse(any(Organization.class))).thenReturn(organizationResponse);
        when(organizationMemberRepository.countByOrganizationId(orgId)).thenReturn(1L);

        organizationService.updateStatus(orgId, OrganizationStatus.SUSPENDED);

        ArgumentCaptor<OrganizationEvent> captor = ArgumentCaptor.forClass(OrganizationEvent.class);
        verify(eventProducer).publish(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(OrganizationEventType.ORGANIZATION_SUSPENDED);
    }

    @Test
    @DisplayName("Delete organization archives it and publishes deleted event")
    void testDeleteOrganization() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));

        organizationService.deleteOrganization(orgId);

        assertThat(organization.getStatus()).isEqualTo(OrganizationStatus.ARCHIVED);
        verify(organizationRepository).save(organization);
        ArgumentCaptor<OrganizationEvent> captor = ArgumentCaptor.forClass(OrganizationEvent.class);
        verify(eventProducer).publish(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(OrganizationEventType.ORGANIZATION_DELETED);
    }
}

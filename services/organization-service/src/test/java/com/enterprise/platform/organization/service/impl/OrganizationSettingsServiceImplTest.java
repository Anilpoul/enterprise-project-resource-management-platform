package com.enterprise.platform.organization.service.impl;

import com.enterprise.platform.organization.dto.request.OrganizationSettingsRequest;
import com.enterprise.platform.organization.dto.response.OrganizationSettingsResponse;
import com.enterprise.platform.organization.entity.Organization;
import com.enterprise.platform.organization.entity.OrganizationSettings;
import com.enterprise.platform.organization.exception.ResourceNotFoundException;
import com.enterprise.platform.organization.mapper.OrganizationSettingsMapper;
import com.enterprise.platform.organization.repository.OrganizationRepository;
import com.enterprise.platform.organization.repository.OrganizationSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationSettingsServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationSettingsRepository settingsRepository;

    @Mock
    private OrganizationSettingsMapper settingsMapper;

    @InjectMocks
    private OrganizationSettingsServiceImpl settingsService;

    private UUID orgId;
    private Organization organization;
    private OrganizationSettings settings;
    private OrganizationSettingsResponse settingsResponse;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();

        organization = new Organization();
        organization.setId(orgId);
        organization.setName("Acme Corp");

        settings = new OrganizationSettings();
        settings.setId(UUID.randomUUID());
        settings.setOrganization(organization);
        settings.setTimezone("UTC");
        settings.setDateFormat("YYYY-MM-DD");
        settings.setMaxProjects(100);
        settings.setMaxUsers(500);

        settingsResponse = OrganizationSettingsResponse.builder()
                .id(settings.getId())
                .organizationId(orgId)
                .timezone("UTC")
                .dateFormat("YYYY-MM-DD")
                .maxProjects(100)
                .maxUsers(500)
                .build();
    }

    @Test
    @DisplayName("Get settings successfully when exists")
    void testGetSettings_Success() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(settingsRepository.findByOrganizationId(orgId)).thenReturn(Optional.of(settings));
        when(settingsMapper.toResponse(settings)).thenReturn(settingsResponse);

        OrganizationSettingsResponse result = settingsService.getSettings(orgId);

        assertThat(result).isNotNull();
        assertThat(result.getTimezone()).isEqualTo("UTC");
    }

    @Test
    @DisplayName("Get settings creates default when absent")
    void testGetSettings_CreatesDefault() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(settingsRepository.findByOrganizationId(orgId)).thenReturn(Optional.empty());
        when(settingsRepository.save(any(OrganizationSettings.class))).thenReturn(settings);
        when(settingsMapper.toResponse(any(OrganizationSettings.class))).thenReturn(settingsResponse);

        OrganizationSettingsResponse result = settingsService.getSettings(orgId);

        assertThat(result).isNotNull();
        verify(settingsRepository).save(any(OrganizationSettings.class));
    }

    @Test
    @DisplayName("Get settings throws ResourceNotFoundException when org not found")
    void testGetSettings_OrgNotFound() {
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> settingsService.getSettings(orgId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Update settings successfully")
    void testUpdateSettings_Success() {
        OrganizationSettingsRequest request = OrganizationSettingsRequest.builder()
                .timezone("America/New_York")
                .dateFormat("MM/DD/YYYY")
                .maxProjects(200)
                .maxUsers(1000)
                .mfaRequired(true)
                .allowExternalSharing(true)
                .build();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(settingsRepository.findByOrganizationId(orgId)).thenReturn(Optional.of(settings));
        when(settingsRepository.save(any(OrganizationSettings.class))).thenReturn(settings);
        when(settingsMapper.toResponse(any(OrganizationSettings.class))).thenReturn(settingsResponse);

        OrganizationSettingsResponse result = settingsService.updateSettings(orgId, request);

        assertThat(result).isNotNull();
        verify(settingsRepository).save(settings);
        assertThat(settings.getTimezone()).isEqualTo("America/New_York");
        assertThat(settings.getDateFormat()).isEqualTo("MM/DD/YYYY");
        assertThat(settings.getMaxProjects()).isEqualTo(200);
        assertThat(settings.getMaxUsers()).isEqualTo(1000);
        assertThat(settings.getMfaRequired()).isTrue();
        assertThat(settings.getAllowExternalSharing()).isTrue();
    }
}

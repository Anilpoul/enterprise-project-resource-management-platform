package com.enterprise.platform.organization.service.impl;

import com.enterprise.platform.organization.dto.request.OrganizationSettingsRequest;
import com.enterprise.platform.organization.dto.response.OrganizationSettingsResponse;
import com.enterprise.platform.organization.entity.Organization;
import com.enterprise.platform.organization.entity.OrganizationSettings;
import com.enterprise.platform.organization.exception.ResourceNotFoundException;
import com.enterprise.platform.organization.mapper.OrganizationSettingsMapper;
import com.enterprise.platform.organization.repository.OrganizationRepository;
import com.enterprise.platform.organization.repository.OrganizationSettingsRepository;
import com.enterprise.platform.organization.service.OrganizationSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationSettingsServiceImpl implements OrganizationSettingsService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationSettingsRepository settingsRepository;
    private final OrganizationSettingsMapper settingsMapper;

    @Override
    @Transactional(readOnly = true)
    public OrganizationSettingsResponse getSettings(UUID organizationId) {
        log.debug("Fetching settings for organization {}", organizationId);
        findOrganizationOrThrow(organizationId);

        OrganizationSettings settings = settingsRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> createDefaultSettings(organizationId));

        return settingsMapper.toResponse(settings);
    }

    @Override
    @Transactional
    public OrganizationSettingsResponse updateSettings(UUID organizationId, OrganizationSettingsRequest request) {
        log.info("Updating settings for organization {}", organizationId);
        Organization organization = findOrganizationOrThrow(organizationId);

        OrganizationSettings settings = settingsRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> {
                    OrganizationSettings newSettings = new OrganizationSettings();
                    newSettings.setOrganization(organization);
                    return newSettings;
                });

        if (request.getTimezone() != null) {
            settings.setTimezone(request.getTimezone());
        }
        if (request.getDateFormat() != null) {
            settings.setDateFormat(request.getDateFormat());
        }
        if (request.getAllowExternalSharing() != null) {
            settings.setAllowExternalSharing(request.getAllowExternalSharing());
        }
        if (request.getMfaRequired() != null) {
            settings.setMfaRequired(request.getMfaRequired());
        }
        if (request.getMaxProjects() != null) {
            settings.setMaxProjects(request.getMaxProjects());
        }
        if (request.getMaxUsers() != null) {
            settings.setMaxUsers(request.getMaxUsers());
        }

        OrganizationSettings saved = settingsRepository.save(settings);
        log.info("Settings updated successfully for organization {}", organizationId);

        return settingsMapper.toResponse(saved);
    }

    private Organization findOrganizationOrThrow(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + id));
    }

    private OrganizationSettings createDefaultSettings(UUID organizationId) {
        Organization org = findOrganizationOrThrow(organizationId);
        OrganizationSettings settings = new OrganizationSettings();
        settings.setOrganization(org);
        return settingsRepository.save(settings);
    }
}

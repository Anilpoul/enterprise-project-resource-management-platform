package com.enterprise.platform.organization.service;

import com.enterprise.platform.organization.dto.request.OrganizationSettingsRequest;
import com.enterprise.platform.organization.dto.response.OrganizationSettingsResponse;

import java.util.UUID;

public interface OrganizationSettingsService {

    OrganizationSettingsResponse getSettings(UUID organizationId);

    OrganizationSettingsResponse updateSettings(UUID organizationId, OrganizationSettingsRequest request);
}

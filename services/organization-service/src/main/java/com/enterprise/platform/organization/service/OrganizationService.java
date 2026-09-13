package com.enterprise.platform.organization.service;

import com.enterprise.platform.organization.constants.enums.OrganizationStatus;
import com.enterprise.platform.organization.dto.request.CreateOrganizationRequest;
import com.enterprise.platform.organization.dto.request.UpdateOrganizationRequest;
import com.enterprise.platform.organization.dto.response.OrganizationResponse;
import com.enterprise.platform.organization.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrganizationService {

    OrganizationResponse createOrganization(CreateOrganizationRequest request);

    OrganizationResponse getOrganizationById(UUID id);

    OrganizationResponse getOrganizationBySlug(String slug);

    OrganizationResponse updateOrganization(UUID id, UpdateOrganizationRequest request);

    PagedResponse<OrganizationResponse> searchOrganizations(String query, Pageable pageable);

    OrganizationResponse updateStatus(UUID id, OrganizationStatus status);

    void deleteOrganization(UUID id);
}

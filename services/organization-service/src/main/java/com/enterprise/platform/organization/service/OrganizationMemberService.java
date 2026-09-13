package com.enterprise.platform.organization.service;

import com.enterprise.platform.organization.dto.request.AddMemberRequest;
import com.enterprise.platform.organization.dto.request.UpdateMemberRoleRequest;
import com.enterprise.platform.organization.dto.response.OrganizationMemberResponse;
import com.enterprise.platform.organization.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrganizationMemberService {

    OrganizationMemberResponse addMember(UUID organizationId, AddMemberRequest request);

    PagedResponse<OrganizationMemberResponse> getMembers(UUID organizationId, Pageable pageable);

    OrganizationMemberResponse getMember(UUID organizationId, UUID userId);

    OrganizationMemberResponse updateMemberRole(UUID organizationId, UUID userId, UpdateMemberRoleRequest request);

    void removeMember(UUID organizationId, UUID userId);
}

package com.enterprise.platform.organization.mapper;

import com.enterprise.platform.organization.dto.response.OrganizationMemberResponse;
import com.enterprise.platform.organization.entity.OrganizationMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganizationMemberMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    OrganizationMemberResponse toResponse(OrganizationMember member);
}

package com.enterprise.platform.organization.mapper;

import com.enterprise.platform.organization.dto.response.OrganizationResponse;
import com.enterprise.platform.organization.entity.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @Mapping(target = "memberCount", expression = "java(organization.getMembers() != null ? organization.getMembers().size() : 0L)")
    OrganizationResponse toResponse(Organization organization);
}

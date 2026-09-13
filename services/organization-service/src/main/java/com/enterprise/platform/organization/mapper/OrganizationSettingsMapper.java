package com.enterprise.platform.organization.mapper;

import com.enterprise.platform.organization.dto.response.OrganizationSettingsResponse;
import com.enterprise.platform.organization.entity.OrganizationSettings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganizationSettingsMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    OrganizationSettingsResponse toResponse(OrganizationSettings settings);
}

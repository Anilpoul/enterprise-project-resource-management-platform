package com.enterprise.platform.resource.mapper;

import com.enterprise.platform.resource.dto.request.AllocateResourceRequest;
import com.enterprise.platform.resource.dto.request.UpdateAllocationRequest;
import com.enterprise.platform.resource.dto.response.ResourceAllocationResponse;
import com.enterprise.platform.resource.entity.ResourceAllocation;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ResourceAllocationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resourceProfile", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "allocatedHoursPerWeek", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ResourceAllocation toEntity(AllocateResourceRequest request);

    @Mapping(source = "resourceProfile.id", target = "resourceId")
    ResourceAllocationResponse toResponse(ResourceAllocation allocation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resourceProfile", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "allocatedHoursPerWeek", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromDto(UpdateAllocationRequest request, @MappingTarget ResourceAllocation allocation);
}

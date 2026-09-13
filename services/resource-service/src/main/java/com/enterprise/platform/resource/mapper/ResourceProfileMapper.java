package com.enterprise.platform.resource.mapper;

import com.enterprise.platform.resource.dto.request.CreateResourceProfileRequest;
import com.enterprise.platform.resource.dto.request.UpdateResourceProfileRequest;
import com.enterprise.platform.resource.dto.response.ResourceProfileResponse;
import com.enterprise.platform.resource.entity.ResourceProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ResourceProfileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "allocations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ResourceProfile toEntity(CreateResourceProfileRequest request);

    @Mapping(target = "totalAllocatedPercentage", ignore = true)
    @Mapping(target = "allocatedHoursPerWeek", ignore = true)
    @Mapping(target = "remainingCapacityHours", ignore = true)
    @Mapping(target = "isOverAllocated", ignore = true)
    ResourceProfileResponse toResponse(ResourceProfile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "allocations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromDto(UpdateResourceProfileRequest request, @MappingTarget ResourceProfile profile);
}

package com.enterprise.platform.user.mapper;

import com.enterprise.platform.user.dto.response.UserProfileResponse;
import com.enterprise.platform.user.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    @Mapping(
            target = "departmentId",
            source = "department.id"
    )
    @Mapping(
            target = "departmentName",
            source = "department.name"
    )
    @Mapping(
            target = "designationId",
            source = "designation.id"
    )
    @Mapping(
            target = "designationName",
            source = "designation.name"
    )
    UserProfileResponse toResponse(
            UserProfile userProfile
    );
}

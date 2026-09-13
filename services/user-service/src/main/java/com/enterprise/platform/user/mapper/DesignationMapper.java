package com.enterprise.platform.user.mapper;

import com.enterprise.platform.user.dto.response.DesignationResponse;
import com.enterprise.platform.user.entity.Designation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DesignationMapper {

    DesignationResponse toResponse(
            Designation designation
    );
}
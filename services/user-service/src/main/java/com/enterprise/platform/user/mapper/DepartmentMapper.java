package com.enterprise.platform.user.mapper;

import com.enterprise.platform.user.dto.response.DepartmentResponse;
import com.enterprise.platform.user.entity.Department;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    DepartmentResponse toResponse(
            Department department
    );
}
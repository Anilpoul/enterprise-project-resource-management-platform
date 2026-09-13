package com.enterprise.platform.project.mapper;

import com.enterprise.platform.project.dto.response.ProjectMemberResponse;
import com.enterprise.platform.project.entity.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    @Mapping(target = "projectId", source = "project.id")
    ProjectMemberResponse toResponse(ProjectMember member);
}

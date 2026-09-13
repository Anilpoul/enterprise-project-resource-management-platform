package com.enterprise.platform.project.mapper;

import com.enterprise.platform.project.dto.response.ProjectMilestoneResponse;
import com.enterprise.platform.project.entity.ProjectMilestone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMilestoneMapper {

    @Mapping(target = "projectId", source = "project.id")
    ProjectMilestoneResponse toResponse(ProjectMilestone milestone);
}

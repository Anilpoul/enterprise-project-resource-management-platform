package com.enterprise.platform.project.mapper;

import com.enterprise.platform.project.dto.response.ProjectResponse;
import com.enterprise.platform.project.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "memberCount", expression = "java(project.getMembers() != null ? project.getMembers().size() : 0L)")
    @Mapping(target = "milestoneCount", expression = "java(project.getMilestones() != null ? project.getMilestones().size() : 0L)")
    ProjectResponse toResponse(Project project);
}

package com.enterprise.platform.project.service;

import com.enterprise.platform.project.constants.enums.ProjectStatus;
import com.enterprise.platform.project.dto.request.CreateProjectRequest;
import com.enterprise.platform.project.dto.request.ProjectSearchCriteria;
import com.enterprise.platform.project.dto.request.UpdateProjectRequest;
import com.enterprise.platform.project.dto.response.PagedResponse;
import com.enterprise.platform.project.dto.response.ProjectResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request);

    ProjectResponse getProjectById(UUID id);

    ProjectResponse getProjectByKey(String projectKey);

    ProjectResponse updateProject(UUID id, UpdateProjectRequest request);

    ProjectResponse updateProjectStatus(UUID id, ProjectStatus status);

    PagedResponse<ProjectResponse> searchProjects(ProjectSearchCriteria criteria, Pageable pageable);

    void deleteProject(UUID id);
}

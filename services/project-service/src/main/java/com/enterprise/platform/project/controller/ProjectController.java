package com.enterprise.platform.project.controller;

import com.enterprise.platform.project.constants.enums.ProjectStatus;
import com.enterprise.platform.project.constants.enums.ProjectType;
import com.enterprise.platform.project.dto.request.CreateProjectRequest;
import com.enterprise.platform.project.dto.request.ProjectSearchCriteria;
import com.enterprise.platform.project.dto.request.UpdateProjectRequest;
import com.enterprise.platform.project.dto.response.ApiResponse;
import com.enterprise.platform.project.dto.response.PagedResponse;
import com.enterprise.platform.project.dto.response.ProjectResponse;
import com.enterprise.platform.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Project Management", description = "APIs for managing projects, keys, and statuses")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request
    ) {
        log.info("REST request to create project: {}", request.getName());
        ProjectResponse response = projectService.createProject(request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Project created successfully"),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable UUID id
    ) {
        log.info("REST request to get project by ID: {}", id);
        ProjectResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/key/{projectKey}")
    @Operation(summary = "Get project by project key")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectByKey(
            @PathVariable String projectKey
    ) {
        log.info("REST request to get project by key: {}", projectKey);
        ProjectResponse response = projectService.getProjectByKey(projectKey);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project details")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        log.info("REST request to update project ID: {}", id);
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Project updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update project status")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProjectStatus(
            @PathVariable UUID id,
            @RequestParam ProjectStatus status
    ) {
        log.info("REST request to update project ID: {} status to {}", id, status);
        ProjectResponse response = projectService.updateProjectStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Project status updated successfully"));
    }

    @GetMapping
    @Operation(summary = "Search and list projects with filters and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<ProjectResponse>>> searchProjects(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) ProjectType projectType,
            @RequestParam(required = false) UUID leadUserId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("REST request to search projects, search: {}, status: {}", search, status);
        ProjectSearchCriteria criteria = ProjectSearchCriteria.builder()
                .search(search)
                .status(status)
                .projectType(projectType)
                .leadUserId(leadUserId)
                .build();

        PagedResponse<ProjectResponse> response = projectService.searchProjects(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Archive/Delete a project")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable UUID id
    ) {
        log.info("REST request to delete project ID: {}", id);
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Project archived successfully"));
    }
}

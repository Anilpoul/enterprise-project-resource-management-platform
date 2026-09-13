package com.enterprise.platform.project.controller;

import com.enterprise.platform.project.dto.request.AddProjectMemberRequest;
import com.enterprise.platform.project.dto.request.UpdateProjectMemberRoleRequest;
import com.enterprise.platform.project.dto.response.ApiResponse;
import com.enterprise.platform.project.dto.response.PagedResponse;
import com.enterprise.platform.project.dto.response.ProjectMemberResponse;
import com.enterprise.platform.project.service.ProjectMemberService;
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
@RequestMapping("/api/v1/projects/{projectId}/members")
@RequiredArgsConstructor
@Tag(name = "Project Member Management", description = "APIs for managing project members and team roles")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @PostMapping
    @Operation(summary = "Add member to project")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody AddProjectMemberRequest request
    ) {
        log.info("REST request to add user {} to project {}", request.getUserId(), projectId);
        ProjectMemberResponse response = projectMemberService.addMember(projectId, request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Project member added successfully"),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(summary = "List project members with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<ProjectMemberResponse>>> getMembers(
            @PathVariable UUID projectId,
            @PageableDefault(size = 20, sort = "joinedAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.info("REST request to get members for project: {}", projectId);
        PagedResponse<ProjectMemberResponse> response = projectMemberService.getMembers(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get specific member in project")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> getMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId
    ) {
        log.info("REST request to get member {} from project {}", userId, projectId);
        ProjectMemberResponse response = projectMemberService.getMember(projectId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{userId}/role")
    @Operation(summary = "Update member role in project")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> updateMemberRole(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateProjectMemberRoleRequest request
    ) {
        log.info("REST request to update role for user {} in project {} to {}", userId, projectId, request.getRole());
        ProjectMemberResponse response = projectMemberService.updateMemberRole(projectId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Member role updated successfully"));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Remove member from project")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId
    ) {
        log.info("REST request to remove user {} from project {}", userId, projectId);
        projectMemberService.removeMember(projectId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Member removed successfully"));
    }
}

package com.enterprise.platform.project.controller;

import com.enterprise.platform.project.dto.request.CreateMilestoneRequest;
import com.enterprise.platform.project.dto.request.UpdateMilestoneRequest;
import com.enterprise.platform.project.dto.response.ApiResponse;
import com.enterprise.platform.project.dto.response.ProjectMilestoneResponse;
import com.enterprise.platform.project.service.ProjectMilestoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectId}/milestones")
@RequiredArgsConstructor
@Tag(name = "Project Milestone Management", description = "APIs for managing project milestones and key deliverables")
public class ProjectMilestoneController {

    private final ProjectMilestoneService milestoneService;

    @PostMapping
    @Operation(summary = "Create milestone for project")
    public ResponseEntity<ApiResponse<ProjectMilestoneResponse>> createMilestone(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateMilestoneRequest request
    ) {
        log.info("REST request to create milestone for project {}", projectId);
        ProjectMilestoneResponse response = milestoneService.createMilestone(projectId, request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Milestone created successfully"),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(summary = "Get all milestones for project")
    public ResponseEntity<ApiResponse<List<ProjectMilestoneResponse>>> getMilestones(
            @PathVariable UUID projectId
    ) {
        log.info("REST request to get milestones for project {}", projectId);
        List<ProjectMilestoneResponse> response = milestoneService.getMilestones(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{milestoneId}")
    @Operation(summary = "Update milestone")
    public ResponseEntity<ApiResponse<ProjectMilestoneResponse>> updateMilestone(
            @PathVariable UUID projectId,
            @PathVariable UUID milestoneId,
            @Valid @RequestBody UpdateMilestoneRequest request
    ) {
        log.info("REST request to update milestone {} for project {}", milestoneId, projectId);
        ProjectMilestoneResponse response = milestoneService.updateMilestone(projectId, milestoneId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Milestone updated successfully"));
    }

    @DeleteMapping("/{milestoneId}")
    @Operation(summary = "Delete milestone")
    public ResponseEntity<ApiResponse<Void>> deleteMilestone(
            @PathVariable UUID projectId,
            @PathVariable UUID milestoneId
    ) {
        log.info("REST request to delete milestone {} for project {}", milestoneId, projectId);
        milestoneService.deleteMilestone(projectId, milestoneId);
        return ResponseEntity.ok(ApiResponse.success(null, "Milestone deleted successfully"));
    }
}

package com.enterprise.platform.sprint.controller;

import com.enterprise.platform.sprint.constants.enums.SprintStatus;
import com.enterprise.platform.sprint.dto.request.CompleteSprintRequest;
import com.enterprise.platform.sprint.dto.request.CreateSprintRequest;
import com.enterprise.platform.sprint.dto.request.StartSprintRequest;
import com.enterprise.platform.sprint.dto.request.UpdateSprintRequest;
import com.enterprise.platform.sprint.dto.response.ApiResponse;
import com.enterprise.platform.sprint.dto.response.SprintResponse;
import com.enterprise.platform.sprint.service.SprintService;
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
@RequiredArgsConstructor
@Tag(name = "Sprint Management", description = "APIs for Scrum Sprints, Iterations, and Velocity Tracking")
public class SprintController {

    private final SprintService sprintService;

    @PostMapping("/api/v1/sprints")
    @Operation(summary = "Create a new sprint")
    public ResponseEntity<ApiResponse<SprintResponse>> createSprint(
            @Valid @RequestBody CreateSprintRequest request
    ) {
        log.info("REST request to create sprint: '{}' for project: {}", request.getName(), request.getProjectId());
        SprintResponse response = sprintService.createSprint(request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Sprint created successfully"),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/api/v1/sprints/{id}/start")
    @Operation(summary = "Start sprint")
    public ResponseEntity<ApiResponse<SprintResponse>> startSprint(
            @PathVariable UUID id,
            @Valid @RequestBody StartSprintRequest request
    ) {
        log.info("REST request to start sprint ID: {}", id);
        SprintResponse response = sprintService.startSprint(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Sprint started successfully"));
    }

    @PostMapping("/api/v1/sprints/{id}/complete")
    @Operation(summary = "Complete sprint")
    public ResponseEntity<ApiResponse<SprintResponse>> completeSprint(
            @PathVariable UUID id,
            @RequestBody(required = false) CompleteSprintRequest request
    ) {
        log.info("REST request to complete sprint ID: {}", id);
        SprintResponse response = sprintService.completeSprint(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Sprint completed successfully"));
    }

    @GetMapping("/api/v1/sprints/{id}")
    @Operation(summary = "Get sprint by ID")
    public ResponseEntity<ApiResponse<SprintResponse>> getSprintById(
            @PathVariable UUID id
    ) {
        log.info("REST request to get sprint by ID: {}", id);
        SprintResponse response = sprintService.getSprintById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/projects/{projectId}/sprints")
    @Operation(summary = "Get sprints for a project with optional status filter")
    public ResponseEntity<ApiResponse<List<SprintResponse>>> getSprintsByProject(
            @PathVariable UUID projectId,
            @RequestParam(required = false) SprintStatus status
    ) {
        log.info("REST request to get sprints for project: {}, status: {}", projectId, status);
        List<SprintResponse> response = sprintService.getSprintsByProject(projectId, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/projects/{projectId}/sprints/active")
    @Operation(summary = "Get active sprint for a project")
    public ResponseEntity<ApiResponse<SprintResponse>> getActiveSprint(
            @PathVariable UUID projectId
    ) {
        log.info("REST request to get active sprint for project: {}", projectId);
        SprintResponse response = sprintService.getActiveSprint(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/api/v1/sprints/{id}")
    @Operation(summary = "Update sprint details")
    public ResponseEntity<ApiResponse<SprintResponse>> updateSprint(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSprintRequest request
    ) {
        log.info("REST request to update sprint ID: {}", id);
        SprintResponse response = sprintService.updateSprint(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Sprint updated successfully"));
    }

    @DeleteMapping("/api/v1/sprints/{id}")
    @Operation(summary = "Delete sprint")
    public ResponseEntity<ApiResponse<Void>> deleteSprint(
            @PathVariable UUID id
    ) {
        log.info("REST request to delete sprint ID: {}", id);
        sprintService.deleteSprint(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sprint deleted successfully"));
    }
}

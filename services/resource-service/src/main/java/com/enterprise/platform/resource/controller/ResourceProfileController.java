package com.enterprise.platform.resource.controller;

import com.enterprise.platform.resource.dto.request.CreateResourceProfileRequest;
import com.enterprise.platform.resource.dto.request.UpdateResourceProfileRequest;
import com.enterprise.platform.resource.dto.response.ApiResponse;
import com.enterprise.platform.resource.dto.response.PagedResponse;
import com.enterprise.platform.resource.dto.response.ResourceProfileResponse;
import com.enterprise.platform.resource.dto.response.WorkloadReportResponse;
import com.enterprise.platform.resource.enums.ResourceStatus;
import com.enterprise.platform.resource.service.ResourceProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "Resource Profile Management", description = "APIs for managing resource profiles, skills, and workload reporting")
public class ResourceProfileController {

    private final ResourceProfileService profileService;

    @PostMapping
    @Operation(summary = "Create a resource profile")
    public ResponseEntity<ApiResponse<ResourceProfileResponse>> createProfile(
            @Valid @RequestBody CreateResourceProfileRequest request
    ) {
        log.info("REST request to create resource profile for user: {}", request.getUserId());
        ResourceProfileResponse response = profileService.createProfile(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Resource profile created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resource profile by ID")
    public ResponseEntity<ApiResponse<ResourceProfileResponse>> getProfileById(@PathVariable UUID id) {
        log.info("REST request to get resource profile: {}", id);
        ResourceProfileResponse response = profileService.getProfileById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get resource profile by User ID")
    public ResponseEntity<ApiResponse<ResourceProfileResponse>> getProfileByUserId(@PathVariable UUID userId) {
        log.info("REST request to get resource profile for user: {}", userId);
        ResourceProfileResponse response = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get paginated resource profiles with optional status filter")
    public ResponseEntity<ApiResponse<PagedResponse<ResourceProfileResponse>>> getProfiles(
            @RequestParam(required = false) ResourceStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.info("REST request to list resource profiles, status: {}", status);
        PagedResponse<ResourceProfileResponse> response = profileService.getProfiles(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/skills/search")
    @Operation(summary = "Search resource profiles by skill keyword")
    public ResponseEntity<ApiResponse<List<ResourceProfileResponse>>> searchBySkill(@RequestParam String skill) {
        log.info("REST request to search resources by skill: {}", skill);
        List<ResourceProfileResponse> response = profileService.searchBySkill(skill);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/reports/workload")
    @Operation(summary = "Get organization workload utilization report")
    public ResponseEntity<ApiResponse<WorkloadReportResponse>> getWorkloadReport() {
        log.info("REST request to get workload report");
        WorkloadReportResponse response = profileService.getWorkloadReport();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update resource profile")
    public ResponseEntity<ApiResponse<ResourceProfileResponse>> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateResourceProfileRequest request
    ) {
        log.info("REST request to update resource profile: {}", id);
        ResourceProfileResponse response = profileService.updateProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Resource profile updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete resource profile")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(@PathVariable UUID id) {
        log.info("REST request to delete resource profile: {}", id);
        profileService.deleteProfile(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Resource profile deleted successfully"));
    }
}

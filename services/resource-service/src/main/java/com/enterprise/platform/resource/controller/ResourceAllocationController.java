package com.enterprise.platform.resource.controller;

import com.enterprise.platform.resource.dto.request.AllocateResourceRequest;
import com.enterprise.platform.resource.dto.request.UpdateAllocationRequest;
import com.enterprise.platform.resource.dto.response.ApiResponse;
import com.enterprise.platform.resource.dto.response.ResourceAllocationResponse;
import com.enterprise.platform.resource.service.ResourceAllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Resource Allocation Management", description = "APIs for allocating resources to projects, workload tracking, and deallocation")
public class ResourceAllocationController {

    private final ResourceAllocationService allocationService;

    @PostMapping("/api/v1/resources/allocations")
    @Operation(summary = "Allocate resource to a project")
    public ResponseEntity<ApiResponse<ResourceAllocationResponse>> allocateResource(
            @Valid @RequestBody AllocateResourceRequest request
    ) {
        log.info("REST request to allocate resource {} to project {}", request.getResourceId(), request.getProjectId());
        ResourceAllocationResponse response = allocationService.allocateResource(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Resource allocated successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/resources/allocations/{id}")
    @Operation(summary = "Get allocation details by ID")
    public ResponseEntity<ApiResponse<ResourceAllocationResponse>> getAllocationById(@PathVariable UUID id) {
        log.info("REST request to get allocation: {}", id);
        ResourceAllocationResponse response = allocationService.getAllocationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/resources/{resourceId}/allocations")
    @Operation(summary = "Get all allocations for a resource profile")
    public ResponseEntity<ApiResponse<List<ResourceAllocationResponse>>> getAllocationsByResourceId(
            @PathVariable UUID resourceId
    ) {
        log.info("REST request to get allocations for resource: {}", resourceId);
        List<ResourceAllocationResponse> response = allocationService.getAllocationsByResourceId(resourceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/projects/{projectId}/allocations")
    @Operation(summary = "Get all allocations for a project")
    public ResponseEntity<ApiResponse<List<ResourceAllocationResponse>>> getAllocationsByProjectId(
            @PathVariable UUID projectId
    ) {
        log.info("REST request to get allocations for project: {}", projectId);
        List<ResourceAllocationResponse> response = allocationService.getAllocationsByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/v1/resources/{resourceId}/cumulative-allocation")
    @Operation(summary = "Get cumulative active allocation percentage for a resource")
    public ResponseEntity<ApiResponse<BigDecimal>> getCumulativeAllocation(@PathVariable UUID resourceId) {
        log.info("REST request to get cumulative allocation for resource: {}", resourceId);
        BigDecimal response = allocationService.getCumulativeAllocation(resourceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/api/v1/resources/allocations/{id}")
    @Operation(summary = "Update resource allocation")
    public ResponseEntity<ApiResponse<ResourceAllocationResponse>> updateAllocation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAllocationRequest request
    ) {
        log.info("REST request to update allocation: {}", id);
        ResourceAllocationResponse response = allocationService.updateAllocation(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Allocation updated successfully"));
    }

    @DeleteMapping("/api/v1/resources/allocations/{id}")
    @Operation(summary = "Deallocate resource from project")
    public ResponseEntity<ApiResponse<Void>> deallocateResource(@PathVariable UUID id) {
        log.info("REST request to deallocate ID: {}", id);
        allocationService.deallocateResource(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Resource deallocated successfully"));
    }
}

package com.enterprise.platform.organization.controller;

import com.enterprise.platform.organization.constants.enums.OrganizationStatus;
import com.enterprise.platform.organization.dto.request.CreateOrganizationRequest;
import com.enterprise.platform.organization.dto.request.UpdateOrganizationRequest;
import com.enterprise.platform.organization.dto.response.ApiResponse;
import com.enterprise.platform.organization.dto.response.OrganizationResponse;
import com.enterprise.platform.organization.dto.response.PagedResponse;
import com.enterprise.platform.organization.service.OrganizationService;
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
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization Management", description = "APIs for managing organizations and multi-tenancy")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @Operation(summary = "Create a new organization")
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request
    ) {
        log.info("REST request to create organization: {}", request.getName());
        OrganizationResponse response = organizationService.createOrganization(request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Organization created successfully"),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(
            @PathVariable UUID id
    ) {
        log.info("REST request to get organization by ID: {}", id);
        OrganizationResponse response = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get organization by slug")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationBySlug(
            @PathVariable String slug
    ) {
        log.info("REST request to get organization by slug: {}", slug);
        OrganizationResponse response = organizationService.getOrganizationBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organization details")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationRequest request
    ) {
        log.info("REST request to update organization ID: {}", id);
        OrganizationResponse response = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Organization updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update organization status")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestParam OrganizationStatus status
    ) {
        log.info("REST request to update organization ID: {} status to {}", id, status);
        OrganizationResponse response = organizationService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Organization status updated successfully"));
    }

    @GetMapping
    @Operation(summary = "Search and list organizations with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<OrganizationResponse>>> searchOrganizations(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("REST request to list organizations, search: {}", search);
        PagedResponse<OrganizationResponse> response = organizationService.searchOrganizations(search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Archive/Delete an organization")
    public ResponseEntity<ApiResponse<Void>> deleteOrganization(
            @PathVariable UUID id
    ) {
        log.info("REST request to delete organization ID: {}", id);
        organizationService.deleteOrganization(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Organization archived successfully"));
    }
}

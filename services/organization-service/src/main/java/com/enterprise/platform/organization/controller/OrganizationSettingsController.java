package com.enterprise.platform.organization.controller;

import com.enterprise.platform.organization.dto.request.OrganizationSettingsRequest;
import com.enterprise.platform.organization.dto.response.ApiResponse;
import com.enterprise.platform.organization.dto.response.OrganizationSettingsResponse;
import com.enterprise.platform.organization.service.OrganizationSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/settings")
@RequiredArgsConstructor
@Tag(name = "Organization Settings Management", description = "APIs for managing organization settings and limits")
public class OrganizationSettingsController {

    private final OrganizationSettingsService settingsService;

    @GetMapping
    @Operation(summary = "Get organization settings")
    public ResponseEntity<ApiResponse<OrganizationSettingsResponse>> getSettings(
            @PathVariable UUID organizationId
    ) {
        log.info("REST request to get settings for organization {}", organizationId);
        OrganizationSettingsResponse response = settingsService.getSettings(organizationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    @Operation(summary = "Update organization settings")
    public ResponseEntity<ApiResponse<OrganizationSettingsResponse>> updateSettings(
            @PathVariable UUID organizationId,
            @Valid @RequestBody OrganizationSettingsRequest request
    ) {
        log.info("REST request to update settings for organization {}", organizationId);
        OrganizationSettingsResponse response = settingsService.updateSettings(organizationId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Settings updated successfully"));
    }
}

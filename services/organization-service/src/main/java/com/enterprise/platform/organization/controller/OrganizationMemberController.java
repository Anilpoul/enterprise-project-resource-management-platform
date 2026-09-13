package com.enterprise.platform.organization.controller;

import com.enterprise.platform.organization.dto.request.AddMemberRequest;
import com.enterprise.platform.organization.dto.request.UpdateMemberRoleRequest;
import com.enterprise.platform.organization.dto.response.ApiResponse;
import com.enterprise.platform.organization.dto.response.OrganizationMemberResponse;
import com.enterprise.platform.organization.dto.response.PagedResponse;
import com.enterprise.platform.organization.service.OrganizationMemberService;
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
@RequestMapping("/api/v1/organizations/{organizationId}/members")
@RequiredArgsConstructor
@Tag(name = "Organization Member Management", description = "APIs for managing organization members and roles")
public class OrganizationMemberController {

    private final OrganizationMemberService memberService;

    @PostMapping
    @Operation(summary = "Add a member to the organization")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> addMember(
            @PathVariable UUID organizationId,
            @Valid @RequestBody AddMemberRequest request
    ) {
        log.info("REST request to add user {} to organization {}", request.getUserId(), organizationId);
        OrganizationMemberResponse response = memberService.addMember(organizationId, request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Member added successfully"),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(summary = "Get list of members in the organization")
    public ResponseEntity<ApiResponse<PagedResponse<OrganizationMemberResponse>>> getMembers(
            @PathVariable UUID organizationId,
            @PageableDefault(size = 20, sort = "joinedAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.info("REST request to get members for organization {}", organizationId);
        PagedResponse<OrganizationMemberResponse> response = memberService.getMembers(organizationId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get member details for a specific user in an organization")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> getMember(
            @PathVariable UUID organizationId,
            @PathVariable UUID userId
    ) {
        log.info("REST request to get member {} from organization {}", userId, organizationId);
        OrganizationMemberResponse response = memberService.getMember(organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{userId}/role")
    @Operation(summary = "Update member role in the organization")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> updateMemberRole(
            @PathVariable UUID organizationId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateMemberRoleRequest request
    ) {
        log.info("REST request to update role for user {} in organization {} to {}", userId, organizationId, request.getRole());
        OrganizationMemberResponse response = memberService.updateMemberRole(organizationId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Member role updated successfully"));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Remove a member from the organization")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID organizationId,
            @PathVariable UUID userId
    ) {
        log.info("REST request to remove user {} from organization {}", userId, organizationId);
        memberService.removeMember(organizationId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Member removed successfully"));
    }
}

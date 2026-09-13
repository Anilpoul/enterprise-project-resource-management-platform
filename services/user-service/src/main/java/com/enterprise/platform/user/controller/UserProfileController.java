package com.enterprise.platform.user.controller;

import com.enterprise.platform.user.dto.request.*;
import com.enterprise.platform.user.dto.response.ApiResponse;
import com.enterprise.platform.user.dto.response.PagedResponse;
import com.enterprise.platform.user.dto.response.UserProfileResponse;
import com.enterprise.platform.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Tag(
        name = "User Profile Management",
        description = "Employee directory and user management APIs"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Operation(
            summary = "Get User Profile",
            description = "Fetch user profile by user ID"
    )
    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getUser(
            @PathVariable UUID userId
    ) {

        return ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("User fetched successfully")
                .data(userProfileService.getUserProfile(userId)
                ).timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Get all User Profiles",
            description = "Fetch all user profiles"
    )
    @GetMapping
    public ApiResponse<PagedResponse<UserProfileResponse>> getUsers(
            Pageable pageable
    ) {

        return ApiResponse
                .<PagedResponse<UserProfileResponse>>builder()
                .success(true)
                .message(
                        "User fetched successfully"
                )
                .data(userProfileService.getUsers(pageable))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Search Users",
            description = "Search users using multiple filters"
    )
    @GetMapping("/search")
    public ApiResponse<PagedResponse<UserProfileResponse>>
    searchUsers(
            UserSearchRequest request,
            Pageable pageable
    ) {

        return ApiResponse
                .<PagedResponse<UserProfileResponse>>builder()
                .success(true)
                .message("Users fetched successfully")
                .data(userProfileService.searchUsers(request, pageable))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Update User Profile"
    )
    @PutMapping("/{userId}")
    public ApiResponse<UserProfileResponse> updateUser(
            @PathVariable UUID userId,
            @Valid
            @RequestBody
            UpdateUserProfileRequest request
    ) {

        return ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("User Updated successfully")
                .data(userProfileService.updateUserProfile(userId, request))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Assign Department"
    )
    @PatchMapping("/{userId}/department")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> assignDepartment(
            @PathVariable UUID userId,
            @RequestBody AssignDepartmentRequest request
    ) {

        userProfileService.assignDepartment(
                userId,
                request.getDepartmentId()
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .message(
                        "Department assigned successfully"
                )
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Assign Designation"
    )
    @PatchMapping("/{userId}/designation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> assignDesignation(
            @PathVariable UUID userId,
            @RequestBody AssignDesignationRequest request
    ) {

        userProfileService.assignDesignation(
                userId,
                request.getDesignationId()
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .message(
                        "Designated assigned successfully"
                )
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Assign Manager"
    )
    @PatchMapping("/{userId}/manager")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> assignManager(
            @PathVariable UUID userId,
            @RequestBody AssignManagerRequest request
    ) {

        userProfileService.assignManager(
                userId,
                request.getManagerId()
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .message(
                        "Manager assigned successfully"
                )
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Generate Employee Code"
    )
    @PostMapping("/{userId}/generate-employee-code")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> generateEmployeeCode(
            @PathVariable UUID userId
    ) {

        userProfileService.generateEmployeeCode(
                userId
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .message(
                        "Employee code generated successfully"
                )
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Activate User"
    )
    @PatchMapping("/{userId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> activateUser(
            @PathVariable UUID userId
    ) {

        userProfileService.activateUser(
                userId
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .message(
                        "User activated successfully"
                )
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Deactivate User"
    )
    @PatchMapping("/{userId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deactivateUser(
            @PathVariable UUID userId
    ) {

        userProfileService.deactivateUser(
                userId
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .message(
                        "User De-activated successfully"
                )
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Suspend User"
    )
    @PatchMapping("/{userId}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> suspendUser(
            @PathVariable UUID userId
    ) {

        userProfileService.suspendUser(
                userId
        );
        return ApiResponse.<Void>builder()
                .success(true)
                .message(
                        "User Suspended successfully"
                )
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Terminate User"
    )
    @PatchMapping("/{userId}/terminate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> terminateUser(
            @PathVariable UUID userId
    ) {

        userProfileService.terminateUser(
                userId
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .message(
                        "User Terminated successfully"
                )
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Assign Organization",
            description = "Assign user to an organization"
    )
    @PatchMapping("/{userId}/organization/{organizationId}")
    public ApiResponse<Void> assignOrganization(
            @PathVariable UUID userId,
            @PathVariable UUID organizationId
    ) {
        userProfileService.assignOrganization(userId, organizationId);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Organization assigned successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }
}

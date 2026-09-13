package com.enterprise.platform.user.controller;

import com.enterprise.platform.user.dto.request.CreateDesignationRequest;
import com.enterprise.platform.user.dto.request.UpdateDesignationRequest;
import com.enterprise.platform.user.dto.response.ApiResponse;
import com.enterprise.platform.user.dto.response.DesignationResponse;
import com.enterprise.platform.user.dto.response.PagedResponse;
import com.enterprise.platform.user.service.DesignationService;
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
        name = "Designation Management",
        description = "Designation CRUD APIs"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/designations")
public class DesignationController {

    private final DesignationService designationService;

    @Operation(
            summary = "Create Designation"
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DesignationResponse> createDesignation(
            @Valid
            @RequestBody
            CreateDesignationRequest request
    ) {

        return ApiResponse.<DesignationResponse>builder()
                .success(true)
                .message("Designation created successfully")
                .data(
                        designationService.createDesignation(
                                request
                        )
                )
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Get Designation"
    )
    @GetMapping("/{designationId}")
    public ApiResponse<DesignationResponse> getDesignation(
            @PathVariable UUID designationId
    ) {

        return ApiResponse.<DesignationResponse>builder()
                .success(true)
                .message("Designation fetched successfully")
                .data(
                        designationService.getDesignation(
                                designationId
                        )
                )
                .timestamp(LocalDateTime.now())
                .build();

    }

    @Operation(
            summary = "Get all Designation"
    )
    @GetMapping
    public ApiResponse<PagedResponse<DesignationResponse>> getDesignations(
            Pageable pageable
    ) {

        return ApiResponse
                .<PagedResponse<DesignationResponse>>builder()
                .success(true)
                .message(
                        "Departments fetched successfully"
                )
                .data(
                        designationService.getDesignations(
                                pageable
                        )
                )
                .timestamp(LocalDateTime.now())
                .build();


    }

    @Operation(
            summary = "Search Designations"
    )
    @GetMapping("/search")
    public PagedResponse<DesignationResponse>  searchDesignations(
            @RequestParam String keyword,
            Pageable pageable
    ) {

        return designationService.searchDesignations(
                keyword,
                pageable
        );
    }

    @Operation(
            summary = "Update Designations"
    )
    @PutMapping("/{designationId}")
    public ApiResponse<DesignationResponse> updateDesignation(
            @PathVariable UUID designationId,
            @Valid
            @RequestBody
            UpdateDesignationRequest request
    ) {

        return ApiResponse.<DesignationResponse>builder()
                .success(true)
                .message("Designation Updated successfully")
                .data(
                        designationService.updateDesignation(
                                designationId,
                                request
                        )
                )
                .timestamp(LocalDateTime.now())
                .build();

    }

    @Operation(
            summary = "Activate Designations"
    )
    @PatchMapping("/{designationId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> activateDesignation(
            @PathVariable UUID designationId
    ) {

        designationService.activateDesignation(
                designationId
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .message(
                        "Designation activated successfully"
                )
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "De-activate Designations"
    )
    @PatchMapping("/{designationId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deactivateDesignation(
            @PathVariable UUID designationId
    ) {

        designationService.deactivateDesignation(
                designationId
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .message(
                        "Department De-activated successfully"
                )
                .timestamp(LocalDateTime.now())
                .build();
    }
}
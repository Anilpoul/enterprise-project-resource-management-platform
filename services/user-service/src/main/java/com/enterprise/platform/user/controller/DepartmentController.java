package com.enterprise.platform.user.controller;

import com.enterprise.platform.user.dto.request.CreateDepartmentRequest;
import com.enterprise.platform.user.dto.request.UpdateDepartmentRequest;
import com.enterprise.platform.user.dto.response.ApiResponse;
import com.enterprise.platform.user.dto.response.DepartmentResponse;
import com.enterprise.platform.user.dto.response.PagedResponse;
import com.enterprise.platform.user.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "Department Management",
        description = "Department CRUD APIs"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(
            summary = "Create Department",
            description = "Creates a new department"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Department created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DepartmentResponse> createDepartment(
            @Valid
            @RequestBody CreateDepartmentRequest request
    ) {

        return ApiResponse.<DepartmentResponse>builder()
                .success(true)
                .message("Department created successfully")
                .data(
                        departmentService.createDepartment(
                                request
                        )
                )
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Get Department",
            description = "Fetch department by ID"
    )
    @GetMapping("/{departmentId}")
    public ApiResponse<DepartmentResponse> getDepartment(
            @PathVariable UUID departmentId
    ) {

        return ApiResponse.<DepartmentResponse>builder()
                .success(true)
                .message("Department fetched successfully")
                .data(
                        departmentService.getDepartment(
                                departmentId
                        )
                )
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Get Departments",
            description = "Fetch all departments"
    )
    @GetMapping
    public ApiResponse<PagedResponse<DepartmentResponse>>
    getDepartments(
            Pageable pageable
    ) {

        return ApiResponse
                .<PagedResponse<DepartmentResponse>>builder()
                .success(true)
                .message(
                        "Departments fetched successfully"
                )
                .data(
                        departmentService.getDepartments(
                                pageable
                        )
                )
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Search Departments",
            description = "Search departments by keyword"
    )
    @GetMapping("/search")
    public PagedResponse<DepartmentResponse> searchDepartments(
            @RequestParam String keyword,
            Pageable pageable
    ) {

        return departmentService.searchDepartments(
                keyword,
                pageable
        );
    }

    @Operation(
            summary = "Update Department",
            description = "Update department by ID"
    )
    @PutMapping("/{departmentId}")
    public ApiResponse<DepartmentResponse> updateDepartment(
            @PathVariable UUID departmentId,
            @Valid
            @RequestBody
            UpdateDepartmentRequest request
    ) {

        return ApiResponse.<DepartmentResponse>builder()
                .success(true)
                .message("Department Updated successfully")
                .data(
                        departmentService.updateDepartment(
                                        departmentId,
                                        request)
                ).timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "Activate Department",
            description = "Activate department by ID"
    )
    @PatchMapping("/{departmentId}/activate")
    public ApiResponse<Void> activateDepartment(
            @PathVariable UUID departmentId
    ) {

        departmentService.activateDepartment(
                departmentId
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .message(
                        "Department activated successfully"
                )
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Operation(
            summary = "De-activate Department",
            description = "De-activate department by ID"
    )
    @PatchMapping("/{departmentId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deactivateDepartment(
            @PathVariable UUID departmentId
    ) {

        return ApiResponse.<Void>builder()
                .success(true)
                .message(
                        "Department De-Activated successfully"
                )
                .timestamp(LocalDateTime.now())
                .build();
    }
}
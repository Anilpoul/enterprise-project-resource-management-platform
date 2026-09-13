package com.enterprise.platform.user.service;

import com.enterprise.platform.user.dto.request.CreateDepartmentRequest;
import com.enterprise.platform.user.dto.request.UpdateDepartmentRequest;
import com.enterprise.platform.user.dto.response.DepartmentResponse;
import com.enterprise.platform.user.dto.response.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DepartmentService {

    DepartmentResponse createDepartment(
            CreateDepartmentRequest request
    );

    DepartmentResponse updateDepartment(
            UUID departmentId,
            UpdateDepartmentRequest request
    );

    DepartmentResponse getDepartment(
            UUID departmentId
    );

    void activateDepartment(
            UUID departmentId
    );

    void deactivateDepartment(
            UUID departmentId
    );

    PagedResponse<DepartmentResponse> getDepartments(
            Pageable pageable
    );

    PagedResponse<DepartmentResponse> searchDepartments(
            String keyword,
            Pageable pageable
    );
}
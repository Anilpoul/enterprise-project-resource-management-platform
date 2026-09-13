package com.enterprise.platform.user.service.impl;

import com.enterprise.platform.user.constants.enums.DepartmentStatus;
import com.enterprise.platform.user.dto.request.CreateDepartmentRequest;
import com.enterprise.platform.user.dto.request.UpdateDepartmentRequest;
import com.enterprise.platform.user.dto.response.DepartmentResponse;
import com.enterprise.platform.user.dto.response.PagedResponse;
import com.enterprise.platform.user.entity.Department;
import com.enterprise.platform.user.exception.BadRequestException;
import com.enterprise.platform.user.exception.ResourceNotFoundException;
import com.enterprise.platform.user.mapper.DepartmentMapper;
import com.enterprise.platform.user.repository.DepartmentRepository;
import com.enterprise.platform.user.service.DepartmentService;
import com.enterprise.platform.user.util.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl
        implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    private final DepartmentMapper departmentMapper;

    @Override
    public DepartmentResponse createDepartment(
            CreateDepartmentRequest request
    ) {

        validateDepartmentName(request.getName());

        Department department = new Department();

        department.setName(
                request.getName().trim()
        );

        department.setDescription(
                request.getDescription()
        );

        department.setStatus(
                DepartmentStatus.ACTIVE
        );

        Department savedDepartment =
                departmentRepository.save(department);

        return departmentMapper.toResponse(
                savedDepartment
        );
    }

    @Override
    public DepartmentResponse updateDepartment(
            UUID departmentId,
            UpdateDepartmentRequest request
    ) {

        Department department =
                getDepartmentEntity(departmentId);

        String requestedName =
                request.getName().trim();

        if (!department.getName()
                .equalsIgnoreCase(requestedName)) {

            validateDepartmentName(requestedName);
        }

        department.setName(requestedName);

        department.setDescription(
                request.getDescription()
        );

        Department updatedDepartment =
                departmentRepository.save(department);

        return departmentMapper.toResponse(
                updatedDepartment
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartment(
            UUID departmentId
    ) {

        Department department =
                getDepartmentEntity(departmentId);

        return departmentMapper.toResponse(
                department
        );
    }

    @Override
    public void activateDepartment(
            UUID departmentId
    ) {

        Department department =
                getDepartmentEntity(departmentId);

        department.setStatus(
                DepartmentStatus.ACTIVE
        );
    }

    @Override
    public void deactivateDepartment(
            UUID departmentId
    ) {

        Department department =
                getDepartmentEntity(departmentId);

        department.setStatus(
                DepartmentStatus.INACTIVE
        );
    }

    private Department getDepartmentEntity(
            UUID departmentId
    ) {

        return departmentRepository
                .findById(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id: "
                                        + departmentId
                        )
                );
    }

    private void validateDepartmentName(
            String departmentName
    ) {

        if (departmentRepository
                .existsByNameIgnoreCase(
                        departmentName.trim()
                )) {

            throw new BadRequestException(
                    "Department already exists: "
                            + departmentName
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DepartmentResponse> getDepartments(
            Pageable pageable
    ) {

        Page<DepartmentResponse> page =
                departmentRepository
                        .findAll(pageable)
                        .map(departmentMapper::toResponse);

        return PageMapper.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DepartmentResponse> searchDepartments(
            String keyword,
            Pageable pageable
    ) {

        Page<DepartmentResponse> page =
                departmentRepository
                        .findByNameContainingIgnoreCase(
                                keyword,
                                pageable
                        )
                        .map(departmentMapper::toResponse);

        return PageMapper.fromPage(page);
    }
}
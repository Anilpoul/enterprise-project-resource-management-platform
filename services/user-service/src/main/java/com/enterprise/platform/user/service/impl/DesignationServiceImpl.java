package com.enterprise.platform.user.service.impl;

import com.enterprise.platform.user.constants.enums.DesignationStatus;
import com.enterprise.platform.user.dto.request.CreateDesignationRequest;
import com.enterprise.platform.user.dto.request.UpdateDesignationRequest;
import com.enterprise.platform.user.dto.response.DesignationResponse;
import com.enterprise.platform.user.dto.response.PagedResponse;
import com.enterprise.platform.user.entity.Designation;
import com.enterprise.platform.user.exception.BadRequestException;
import com.enterprise.platform.user.exception.ResourceNotFoundException;
import com.enterprise.platform.user.mapper.DesignationMapper;
import com.enterprise.platform.user.repository.DesignationRepository;
import com.enterprise.platform.user.service.DesignationService;
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
public class DesignationServiceImpl
        implements DesignationService {

    private final DesignationRepository designationRepository;

    private final DesignationMapper designationMapper;

    @Override
    public DesignationResponse createDesignation(
            CreateDesignationRequest request
    ) {

        validateDesignationName(
                request.getName()
        );

        Designation designation =
                new Designation();

        designation.setName(
                request.getName().trim()
        );

        designation.setDescription(
                request.getDescription()
        );

        designation.setLevel(
                request.getLevel()
        );

        designation.setStatus(
                DesignationStatus.ACTIVE
        );

        return designationMapper.toResponse(
                designationRepository.save(
                        designation
                )
        );
    }

    @Override
    public DesignationResponse updateDesignation(
            UUID designationId,
            UpdateDesignationRequest request
    ) {

        Designation designation =
                getDesignationEntity(
                        designationId
                );

        if (!designation.getName()
                .equalsIgnoreCase(
                        request.getName()
                )) {

            validateDesignationName(
                    request.getName()
            );
        }

        designation.setName(
                request.getName().trim()
        );

        designation.setDescription(
                request.getDescription()
        );

        designation.setLevel(
                request.getLevel()
        );

        return designationMapper.toResponse(
                designation
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DesignationResponse getDesignation(
            UUID designationId
    ) {

        return designationMapper.toResponse(
                getDesignationEntity(
                        designationId
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DesignationResponse> getDesignations(
            Pageable pageable
    ) {

        Page<DesignationResponse> page =
                designationRepository
                        .findAll(pageable)
                        .map(designationMapper::toResponse);

        return PageMapper.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DesignationResponse> searchDesignations(
            String keyword,
            Pageable pageable
    ) {

        Page<DesignationResponse> page =
                designationRepository
                        .findByNameContainingIgnoreCase(
                                keyword,
                                pageable
                        )
                        .map(designationMapper::toResponse);

        return PageMapper.fromPage(page);
    }

    @Override
    public void activateDesignation(
            UUID designationId
    ) {

        getDesignationEntity(
                designationId
        ).setStatus(
                DesignationStatus.ACTIVE
        );
    }

    @Override
    public void deactivateDesignation(
            UUID designationId
    ) {

        getDesignationEntity(
                designationId
        ).setStatus(
                DesignationStatus.INACTIVE
        );
    }

    private Designation getDesignationEntity(
            UUID designationId
    ) {

        return designationRepository
                .findById(
                        designationId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Designation not found"
                        )
                );
    }

    private void validateDesignationName(
            String name
    ) {

        if (designationRepository
                .existsByNameIgnoreCase(
                        name.trim()
                )) {

            throw new BadRequestException(
                    "Designation already exists"
            );
        }
    }
}
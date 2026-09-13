package com.enterprise.platform.user.service;

import com.enterprise.platform.user.dto.request.CreateDesignationRequest;
import com.enterprise.platform.user.dto.request.UpdateDesignationRequest;
import com.enterprise.platform.user.dto.response.DesignationResponse;
import com.enterprise.platform.user.dto.response.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DesignationService {

    DesignationResponse createDesignation(
            CreateDesignationRequest request
    );

    DesignationResponse updateDesignation(
            UUID designationId,
            UpdateDesignationRequest request
    );

    DesignationResponse getDesignation(
            UUID designationId
    );

    PagedResponse<DesignationResponse> getDesignations(
            Pageable pageable
    );

    PagedResponse<DesignationResponse> searchDesignations(
            String keyword,
            Pageable pageable
    );

    void activateDesignation(
            UUID designationId
    );

    void deactivateDesignation(
            UUID designationId
    );
}
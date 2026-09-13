package com.enterprise.platform.resource.service;

import com.enterprise.platform.resource.dto.request.CreateResourceProfileRequest;
import com.enterprise.platform.resource.dto.request.UpdateResourceProfileRequest;
import com.enterprise.platform.resource.dto.response.PagedResponse;
import com.enterprise.platform.resource.dto.response.ResourceProfileResponse;
import com.enterprise.platform.resource.dto.response.WorkloadReportResponse;
import com.enterprise.platform.resource.enums.ResourceStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ResourceProfileService {

    ResourceProfileResponse createProfile(CreateResourceProfileRequest request);

    ResourceProfileResponse getProfileById(UUID id);

    ResourceProfileResponse getProfileByUserId(UUID userId);

    PagedResponse<ResourceProfileResponse> getProfiles(ResourceStatus status, Pageable pageable);

    List<ResourceProfileResponse> searchBySkill(String skill);

    ResourceProfileResponse updateProfile(UUID id, UpdateResourceProfileRequest request);

    void deleteProfile(UUID id);

    WorkloadReportResponse getWorkloadReport();
}

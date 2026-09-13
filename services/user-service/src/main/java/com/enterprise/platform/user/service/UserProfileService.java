package com.enterprise.platform.user.service;

import com.enterprise.platform.events.AuthEvent;
import com.enterprise.platform.user.dto.request.UpdateUserProfileRequest;
import com.enterprise.platform.user.dto.request.UserSearchRequest;
import com.enterprise.platform.user.dto.response.PagedResponse;
import com.enterprise.platform.user.dto.response.UserProfileResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserProfileService {

    void createUserProfile(AuthEvent event);

    UserProfileResponse getUserProfile(
            UUID userId
    );

    PagedResponse<UserProfileResponse> getUsers(
            Pageable pageable
    );

    PagedResponse<UserProfileResponse> searchUsers(
            UserSearchRequest request,
            Pageable pageable
    );

    UserProfileResponse updateUserProfile(
            UUID userId,
            UpdateUserProfileRequest request
    );

    void assignDepartment(
            UUID userId,
            UUID departmentId
    );

    void assignDesignation(
            UUID userId,
            UUID designationId
    );

    void assignManager(
            UUID userId,
            UUID managerId
    );

    void generateEmployeeCode(
            UUID userId
    );

    void activateUser(UUID userId);

    void deactivateUser(UUID userId);

    void suspendUser(UUID userId);

    void terminateUser(UUID userId);

    void assignOrganization(UUID userId, UUID organizationId);
}

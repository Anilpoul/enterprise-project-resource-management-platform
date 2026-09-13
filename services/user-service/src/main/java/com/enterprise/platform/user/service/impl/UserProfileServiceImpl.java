package com.enterprise.platform.user.service.impl;

import com.enterprise.platform.user.constants.enums.UserProfileStatus;
import com.enterprise.platform.user.dto.request.UpdateUserProfileRequest;
import com.enterprise.platform.user.dto.request.UserSearchRequest;
import com.enterprise.platform.user.dto.response.PagedResponse;
import com.enterprise.platform.user.dto.response.UserProfileResponse;
import com.enterprise.platform.user.entity.Department;
import com.enterprise.platform.user.entity.Designation;
import com.enterprise.platform.user.entity.UserProfile;
import com.enterprise.platform.events.AuthEvent;
import com.enterprise.platform.user.exception.BadRequestException;
import com.enterprise.platform.user.exception.ResourceNotFoundException;
import com.enterprise.platform.user.mapper.UserProfileMapper;
import com.enterprise.platform.user.repository.DepartmentRepository;
import com.enterprise.platform.user.repository.DesignationRepository;
import com.enterprise.platform.user.repository.UserProfileRepository;
import com.enterprise.platform.user.service.EmployeeCodeGenerator;
import com.enterprise.platform.user.service.UserProfileService;
import com.enterprise.platform.user.specification.UserProfileSpecification;
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
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;

    private final DepartmentRepository departmentRepository;

    private final DesignationRepository designationRepository;

    private final UserProfileMapper userProfileMapper;

    private final EmployeeCodeGenerator employeeCodeGenerator;

    @Override
    public void createUserProfile(AuthEvent event) {

        if (userProfileRepository
                .findByUserId(event.getUserId())
                .isPresent()) {

            return;
        }

        UserProfile profile = new UserProfile();

        profile.setUserId(event.getUserId());

        profile.setFirstName(event.getFirstName());

        profile.setLastName(event.getLastName());

        profile.setEmail(event.getEmail());

        profile.setEmployeeCode(employeeCodeGenerator.generate());

        profile.setStatus(UserProfileStatus.ACTIVE);

        userProfileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(
            UUID userId
    ) {

        return userProfileMapper.toResponse(
                getUser(userId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserProfileResponse> getUsers(
            Pageable pageable
    ) {

        Page<UserProfileResponse> page =
                userProfileRepository
                        .findAll(pageable)
                        .map(userProfileMapper::toResponse);

        return PageMapper.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserProfileResponse> searchUsers(
            UserSearchRequest request,
            Pageable pageable
    ) {

        Page<UserProfileResponse> page =
                userProfileRepository
                        .findAll(
                                UserProfileSpecification
                                        .search(request),
                                pageable
                        )
                        .map(
                                userProfileMapper::toResponse
                        );

        return PageMapper.fromPage(
                page
        );
    }

    @Override
    public UserProfileResponse updateUserProfile(
            UUID userId,
            UpdateUserProfileRequest request
    ) {

        UserProfile profile =
                getUser(userId);

        profile.setFirstName(
                request.getFirstName()
        );

        profile.setLastName(
                request.getLastName()
        );

        profile.setPhoneNumber(
                request.getPhoneNumber()
        );

        profile.setJoiningDate(
                request.getJoiningDate()
        );

        profile.setProfileImageUrl(
                request.getProfileImageUrl()
        );

        if (request.getOrganizationId() != null) {
            profile.setOrganizationId(
                    request.getOrganizationId()
            );
        }

        return userProfileMapper.toResponse(
                profile
        );
    }

    @Override
    public void assignDepartment(
            UUID userId,
            UUID departmentId
    ) {

        UserProfile profile =
                getUser(userId);

        Department department =
                departmentRepository.findById(departmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Department not found"
                                )
                        );

        profile.setDepartment(
                department
        );
    }

    @Override
    public void assignDesignation(
            UUID userId,
            UUID designationId
    ) {

        UserProfile profile =
                getUser(userId);

        Designation designation =
                designationRepository.findById(designationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Designation not found"
                                )
                        );

        profile.setDesignation(
                designation
        );
    }

    @Override
    public void assignManager(
            UUID userId,
            UUID managerId
    ) {

        UserProfile profile =
                getUser(userId);

        UserProfile manager =
                getUser(managerId);

        if (profile.getUserId()
                .equals(manager.getUserId())) {

            throw new BadRequestException(
                    "User cannot be own manager"
            );
        }

        profile.setManagerId(
                manager.getUserId()
        );
    }

    @Override
    public void generateEmployeeCode(
            UUID userId
    ) {

        UserProfile profile =
                getUser(userId);

        if (profile.getEmployeeCode() != null) {

            return;
        }

        profile.setEmployeeCode(
                employeeCodeGenerator.generate()
        );
    }

    private UserProfile getUser(
            UUID userId
    ) {

        return userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User profile not found"
                        )
                );
    }

    @Override
    public void activateUser(UUID userId) {

        UserProfile userProfile =
                getUser(userId);

        if (userProfile.getStatus() ==
                UserProfileStatus.TERMINATED) {

            throw new BadRequestException(
                    "Terminated user cannot be activated"
            );
        }

        userProfile.setStatus(
                UserProfileStatus.ACTIVE
        );
    }

    @Override
    public void deactivateUser(UUID userId) {

        UserProfile userProfile =
                getUser(userId);

        if (userProfile.getStatus() ==
                UserProfileStatus.TERMINATED) {

            throw new BadRequestException(
                    "Terminated user cannot be deactivated"
            );
        }

        userProfile.setStatus(
                UserProfileStatus.INACTIVE
        );
    }

    @Override
    public void suspendUser(UUID userId) {

        UserProfile userProfile =
                getUser(userId);

        if (userProfile.getStatus() ==
                UserProfileStatus.TERMINATED) {

            throw new BadRequestException(
                    "Terminated user cannot be suspended"
            );
        }

        userProfile.setStatus(
                UserProfileStatus.SUSPENDED
        );
    }

    @Override
    public void terminateUser(UUID userId) {

        UserProfile userProfile =
                getUser(userId);

        userProfile.setStatus(
                UserProfileStatus.TERMINATED
        );
    }

    @Override
    public void assignOrganization(UUID userId, UUID organizationId) {
        UserProfile userProfile = getUser(userId);
        userProfile.setOrganizationId(organizationId);
    }
}

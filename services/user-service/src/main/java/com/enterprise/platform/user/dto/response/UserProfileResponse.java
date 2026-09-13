package com.enterprise.platform.user.dto.response;

import com.enterprise.platform.user.constants.enums.UserProfileStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class UserProfileResponse {

    private UUID id;

    private UUID userId;

    private String employeeCode;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String profileImageUrl;

    private LocalDate joiningDate;

    private UserProfileStatus status;

    private UUID departmentId;

    private String departmentName;

    private UUID designationId;

    private String designationName;

    private UUID managerId;

    private UUID organizationId;
}

package com.enterprise.platform.user.dto.request;

import com.enterprise.platform.user.constants.enums.UserProfileStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserSearchRequest {

    private String keyword;

    private UUID departmentId;

    private UUID designationId;

    private UUID managerId;

    private UserProfileStatus status;

    private String employeeCode;

    private UUID organizationId;
}
package com.enterprise.platform.user.dto.response;

import com.enterprise.platform.user.constants.enums.DepartmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class DepartmentResponse {

    private UUID id;

    private String name;

    private String description;

    private DepartmentStatus status;
}
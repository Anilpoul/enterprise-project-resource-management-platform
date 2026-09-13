package com.enterprise.platform.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AssignDepartmentRequest {

    @NotNull
    private UUID departmentId;
}

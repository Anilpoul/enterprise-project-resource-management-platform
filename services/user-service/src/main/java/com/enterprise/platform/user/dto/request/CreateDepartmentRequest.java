package com.enterprise.platform.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
public class CreateDepartmentRequest {

    @Schema(
            example = "Engineering",
            description = "Department name"
    )
    @NotBlank
    @Size(max = 100)
    private String name;

    @Schema(
            example = "Engineering Department"
    )
    @Size(max = 500)
    private String description;
}

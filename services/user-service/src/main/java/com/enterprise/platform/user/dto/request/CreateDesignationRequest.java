package com.enterprise.platform.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDesignationRequest {

    @Schema(
            example = "Senior Software Engineer"
    )
    @NotBlank
    private String name;

    private String description;

    @Schema(
            example = "5"
    )
    @Min(1)
    private Integer level;
}
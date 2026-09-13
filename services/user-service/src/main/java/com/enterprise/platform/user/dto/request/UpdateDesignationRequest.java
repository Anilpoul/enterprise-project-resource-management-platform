package com.enterprise.platform.user.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDesignationRequest {

    @NotBlank
    private String name;

    private String description;

    @Min(1)
    private Integer level;
}
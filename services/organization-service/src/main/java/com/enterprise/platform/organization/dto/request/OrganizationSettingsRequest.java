package com.enterprise.platform.organization.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationSettingsRequest {

    @Size(max = 50)
    private String timezone;

    @Size(max = 50)
    private String dateFormat;

    private Boolean allowExternalSharing;

    private Boolean mfaRequired;

    @Min(1)
    @Max(10000)
    private Integer maxProjects;

    @Min(1)
    @Max(50000)
    private Integer maxUsers;
}

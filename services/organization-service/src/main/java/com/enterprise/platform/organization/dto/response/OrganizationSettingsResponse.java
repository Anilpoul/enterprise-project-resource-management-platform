package com.enterprise.platform.organization.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationSettingsResponse {

    private UUID id;

    private UUID organizationId;

    private String timezone;

    private String dateFormat;

    private Boolean allowExternalSharing;

    private Boolean mfaRequired;

    private Integer maxProjects;

    private Integer maxUsers;

    private LocalDateTime updatedAt;
}

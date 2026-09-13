package com.enterprise.platform.organization.dto.response;

import com.enterprise.platform.organization.constants.enums.OrganizationStatus;
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
public class OrganizationResponse {

    private UUID id;

    private String name;

    private String slug;

    private String description;

    private String logoUrl;

    private OrganizationStatus status;

    private long memberCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

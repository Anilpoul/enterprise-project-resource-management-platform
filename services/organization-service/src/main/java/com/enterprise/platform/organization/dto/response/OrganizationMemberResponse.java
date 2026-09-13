package com.enterprise.platform.organization.dto.response;

import com.enterprise.platform.organization.constants.enums.MemberStatus;
import com.enterprise.platform.organization.constants.enums.OrganizationRole;
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
public class OrganizationMemberResponse {

    private UUID id;

    private UUID organizationId;

    private UUID userId;

    private OrganizationRole role;

    private MemberStatus status;

    private LocalDateTime joinedAt;

    private LocalDateTime createdAt;
}

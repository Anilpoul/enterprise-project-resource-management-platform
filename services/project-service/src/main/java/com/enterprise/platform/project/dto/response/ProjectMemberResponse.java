package com.enterprise.platform.project.dto.response;

import com.enterprise.platform.project.constants.enums.MemberStatus;
import com.enterprise.platform.project.constants.enums.ProjectRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberResponse {

    private UUID id;

    private UUID projectId;

    private UUID organizationId;

    private UUID userId;

    private ProjectRole role;

    private MemberStatus status;

    private LocalDateTime joinedAt;

    private LocalDateTime createdAt;
}

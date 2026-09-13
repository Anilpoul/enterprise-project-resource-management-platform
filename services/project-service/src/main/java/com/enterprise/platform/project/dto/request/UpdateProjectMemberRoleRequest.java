package com.enterprise.platform.project.dto.request;

import com.enterprise.platform.project.constants.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProjectMemberRoleRequest {

    @NotNull(message = "Role is required")
    private ProjectRole role;
}

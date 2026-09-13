package com.enterprise.platform.project.dto.request;

import com.enterprise.platform.project.constants.enums.ProjectStatus;
import com.enterprise.platform.project.constants.enums.ProjectType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectSearchCriteria {

    private String search;

    private ProjectStatus status;

    private ProjectType projectType;

    private UUID leadUserId;
}

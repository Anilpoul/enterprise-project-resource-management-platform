package com.enterprise.platform.project.dto.request;

import com.enterprise.platform.project.constants.enums.ProjectStatus;
import com.enterprise.platform.project.constants.enums.ProjectType;
import com.enterprise.platform.project.constants.enums.ProjectVisibility;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProjectRequest {

    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private ProjectType projectType;

    private ProjectVisibility visibility;

    private ProjectStatus status;

    private UUID leadUserId;

    private LocalDate startDate;

    private LocalDate targetEndDate;

    private LocalDate actualEndDate;

    private BigDecimal budget;

    private String currency;
}

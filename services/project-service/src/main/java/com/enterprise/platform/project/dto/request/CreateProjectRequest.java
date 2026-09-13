package com.enterprise.platform.project.dto.request;

import com.enterprise.platform.project.constants.enums.ProjectType;
import com.enterprise.platform.project.constants.enums.ProjectVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    @NotBlank(message = "Project key is required")
    @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "Project key must be 2 to 10 uppercase alphanumeric characters")
    private String projectKey;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Project type is required")
    private ProjectType projectType;

    private ProjectVisibility visibility;

    @NotNull(message = "Lead user ID is required")
    private UUID leadUserId;

    private LocalDate startDate;

    private LocalDate targetEndDate;

    private BigDecimal budget;

    private String currency;
}

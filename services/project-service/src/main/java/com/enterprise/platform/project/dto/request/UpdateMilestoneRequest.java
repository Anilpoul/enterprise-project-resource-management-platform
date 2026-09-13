package com.enterprise.platform.project.dto.request;

import com.enterprise.platform.project.constants.enums.MilestoneStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMilestoneRequest {

    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private LocalDate dueDate;

    private MilestoneStatus status;
}

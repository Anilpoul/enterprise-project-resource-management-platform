package com.enterprise.platform.resource.dto.request;

import com.enterprise.platform.resource.enums.ResourceStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateResourceProfileRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Job title is required")
    @Size(max = 150, message = "Job title cannot exceed 150 characters")
    private String jobTitle;

    @DecimalMin(value = "1.00", message = "Weekly capacity hours must be at least 1.00")
    @DecimalMax(value = "168.00", message = "Weekly capacity hours cannot exceed 168.00")
    private BigDecimal weeklyCapacityHours;

    private String skills;

    @DecimalMin(value = "0.00", message = "Hourly rate cannot be negative")
    private BigDecimal hourlyRate;

    @Size(max = 10, message = "Currency code cannot exceed 10 characters")
    private String currency;

    private ResourceStatus status;
}

package com.enterprise.platform.resource.dto.request;

import com.enterprise.platform.resource.enums.AllocationStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocateResourceRequest {

    @NotNull(message = "Resource ID is required")
    private UUID resourceId;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotNull(message = "Allocation percentage is required")
    @DecimalMin(value = "0.01", message = "Allocation percentage must be greater than 0")
    @DecimalMax(value = "100.00", message = "Single allocation percentage cannot exceed 100.00%")
    private BigDecimal allocationPercentage;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private AllocationStatus status;

    private String notes;
}

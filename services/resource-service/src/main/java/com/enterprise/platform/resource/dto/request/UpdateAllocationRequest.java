package com.enterprise.platform.resource.dto.request;

import com.enterprise.platform.resource.enums.AllocationStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAllocationRequest {

    @DecimalMin(value = "0.01", message = "Allocation percentage must be greater than 0")
    @DecimalMax(value = "100.00", message = "Single allocation percentage cannot exceed 100.00%")
    private BigDecimal allocationPercentage;

    private LocalDate startDate;

    private LocalDate endDate;

    private AllocationStatus status;

    private String notes;
}

package com.enterprise.platform.resource.dto.response;

import com.enterprise.platform.resource.enums.AllocationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceAllocationResponse {

    private UUID id;
    private UUID resourceId;
    private UUID organizationId;
    private UUID projectId;
    private BigDecimal allocationPercentage;
    private BigDecimal allocatedHoursPerWeek;
    private LocalDate startDate;
    private LocalDate endDate;
    private AllocationStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

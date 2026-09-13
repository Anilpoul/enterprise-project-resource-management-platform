package com.enterprise.platform.resource.dto.response;

import com.enterprise.platform.resource.enums.ResourceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceUtilizationDto {

    private UUID resourceId;
    private UUID userId;
    private String jobTitle;
    private BigDecimal weeklyCapacityHours;
    private BigDecimal totalAllocatedPercentage;
    private BigDecimal allocatedHoursPerWeek;
    private BigDecimal remainingCapacityHours;
    private ResourceStatus status;
    private boolean isOverAllocated;
    private int activeAllocationsCount;
}

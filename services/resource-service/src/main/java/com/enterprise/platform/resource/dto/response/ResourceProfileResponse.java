package com.enterprise.platform.resource.dto.response;

import com.enterprise.platform.resource.enums.ResourceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceProfileResponse {

    private UUID id;
    private UUID organizationId;
    private UUID userId;
    private String jobTitle;
    private BigDecimal weeklyCapacityHours;
    private String skills;
    private BigDecimal hourlyRate;
    private String currency;
    private ResourceStatus status;
    private BigDecimal totalAllocatedPercentage;
    private BigDecimal allocatedHoursPerWeek;
    private BigDecimal remainingCapacityHours;
    private Boolean isOverAllocated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

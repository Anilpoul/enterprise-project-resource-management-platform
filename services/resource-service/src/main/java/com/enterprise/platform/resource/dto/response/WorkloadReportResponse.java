package com.enterprise.platform.resource.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadReportResponse {

    private UUID organizationId;
    private int totalResources;
    private int totalAllocatedResources;
    private int overAllocatedResources;
    private BigDecimal averageUtilizationPercentage;
    private List<ResourceUtilizationDto> resources;
}

package com.enterprise.platform.analytics.dto.response;

import com.enterprise.platform.analytics.enums.PerformanceRating;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeScorecardResponse {

    private UUID id;
    private UUID userId;
    private Integer tasksAssigned;
    private Integer tasksCompleted;
    private Integer tasksOverdue;
    private Integer storyPointsDelivered;
    private BigDecimal onTimeCompletionRate;
    private BigDecimal performanceScore;
    private PerformanceRating performanceRating;
}

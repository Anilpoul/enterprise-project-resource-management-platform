package com.enterprise.platform.analytics.entity;

import com.enterprise.platform.analytics.enums.PerformanceRating;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "employee_performance_metrics", uniqueConstraints = {
        @UniqueConstraint(name = "uk_emp_perf_org_user", columnNames = {"organization_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePerformanceMetric extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Builder.Default
    @Column(name = "tasks_assigned", nullable = false)
    private Integer tasksAssigned = 0;

    @Builder.Default
    @Column(name = "tasks_completed", nullable = false)
    private Integer tasksCompleted = 0;

    @Builder.Default
    @Column(name = "tasks_overdue", nullable = false)
    private Integer tasksOverdue = 0;

    @Builder.Default
    @Column(name = "story_points_delivered", nullable = false)
    private Integer storyPointsDelivered = 0;

    @Builder.Default
    @Column(name = "on_time_completion_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal onTimeCompletionRate = new BigDecimal("100.00");

    @Builder.Default
    @Column(name = "performance_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal performanceScore = new BigDecimal("100.00");

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "performance_rating", nullable = false, length = 30)
    private PerformanceRating performanceRating = PerformanceRating.EXCELLENT;
}

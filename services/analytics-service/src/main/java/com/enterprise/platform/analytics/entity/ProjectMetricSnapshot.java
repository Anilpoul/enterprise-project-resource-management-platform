package com.enterprise.platform.analytics.entity;

import com.enterprise.platform.analytics.enums.ProjectHealthStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "project_metric_snapshots", uniqueConstraints = {
        @UniqueConstraint(name = "uk_proj_metrics_org_proj", columnNames = {"organization_id", "project_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMetricSnapshot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Builder.Default
    @Column(name = "total_tasks", nullable = false)
    private Integer totalTasks = 0;

    @Builder.Default
    @Column(name = "completed_tasks", nullable = false)
    private Integer completedTasks = 0;

    @Builder.Default
    @Column(name = "in_progress_tasks", nullable = false)
    private Integer inProgressTasks = 0;

    @Builder.Default
    @Column(name = "blocked_tasks", nullable = false)
    private Integer blockedTasks = 0;

    @Builder.Default
    @Column(name = "overdue_tasks", nullable = false)
    private Integer overdueTasks = 0;

    @Builder.Default
    @Column(name = "total_story_points", nullable = false)
    private Integer totalStoryPoints = 0;

    @Builder.Default
    @Column(name = "completed_story_points", nullable = false)
    private Integer completedStoryPoints = 0;

    @Builder.Default
    @Column(name = "completion_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal completionRate = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", nullable = false, length = 30)
    private ProjectHealthStatus healthStatus = ProjectHealthStatus.ON_TRACK;
}

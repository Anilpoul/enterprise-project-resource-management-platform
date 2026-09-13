package com.enterprise.platform.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sprint_metric_snapshots", uniqueConstraints = {
        @UniqueConstraint(name = "uk_sprint_metrics_org_sprint", columnNames = {"organization_id", "sprint_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintMetricSnapshot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "sprint_id", nullable = false)
    private UUID sprintId;

    @Column(name = "sprint_name", length = 150)
    private String sprintName;

    @Builder.Default
    @Column(name = "committed_story_points", nullable = false)
    private Integer committedStoryPoints = 0;

    @Builder.Default
    @Column(name = "completed_story_points", nullable = false)
    private Integer completedStoryPoints = 0;

    @Builder.Default
    @Column(name = "velocity", nullable = false, precision = 5, scale = 2)
    private BigDecimal velocity = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "completion_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal completionRate = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "spillover_tasks_count", nullable = false)
    private Integer spilloverTasksCount = 0;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "COMPLETED";
}

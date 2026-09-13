package com.enterprise.platform.project.entity;

import com.enterprise.platform.project.constants.enums.MilestoneStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "project_milestones",
        indexes = {
                @Index(name = "idx_milestone_proj_id", columnList = "project_id"),
                @Index(name = "idx_milestone_org_id", columnList = "organization_id"),
                @Index(name = "idx_milestone_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ProjectMilestone extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MilestoneStatus status;
}

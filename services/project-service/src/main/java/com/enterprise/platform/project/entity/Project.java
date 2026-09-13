package com.enterprise.platform.project.entity;

import com.enterprise.platform.project.constants.enums.ProjectStatus;
import com.enterprise.platform.project.constants.enums.ProjectType;
import com.enterprise.platform.project.constants.enums.ProjectVisibility;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "projects",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_org_project_key", columnNames = {"organization_id", "project_key"}),
                @UniqueConstraint(name = "uq_org_project_name", columnNames = {"organization_id", "name"})
        },
        indexes = {
                @Index(name = "idx_projects_org_id", columnList = "organization_id"),
                @Index(name = "idx_projects_status", columnList = "status"),
                @Index(name = "idx_projects_lead", columnList = "lead_user_id"),
                @Index(name = "idx_projects_key", columnList = "project_key")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Project extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "project_key", nullable = false, length = 10)
    private String projectKey;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = false, length = 50)
    private ProjectType projectType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProjectStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProjectVisibility visibility;

    @Column(name = "lead_user_id", nullable = false)
    private UUID leadUserId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "target_end_date")
    private LocalDate targetEndDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal budget;

    @Column(nullable = false, length = 10)
    private String currency = "USD";

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectMilestone> milestones = new ArrayList<>();
}

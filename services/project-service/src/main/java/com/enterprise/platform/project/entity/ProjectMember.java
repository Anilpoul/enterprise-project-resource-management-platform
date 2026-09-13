package com.enterprise.platform.project.entity;

import com.enterprise.platform.project.constants.enums.MemberStatus;
import com.enterprise.platform.project.constants.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "project_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_project_member", columnNames = {"project_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_proj_member_proj_id", columnList = "project_id"),
                @Index(name = "idx_proj_member_org_id", columnList = "organization_id"),
                @Index(name = "idx_proj_member_user_id", columnList = "user_id"),
                @Index(name = "idx_proj_member_role", columnList = "role")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ProjectMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProjectRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MemberStatus status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}

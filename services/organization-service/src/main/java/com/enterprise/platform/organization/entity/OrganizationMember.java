package com.enterprise.platform.organization.entity;

import com.enterprise.platform.organization.constants.enums.MemberStatus;
import com.enterprise.platform.organization.constants.enums.OrganizationRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "organization_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_org_member", columnNames = {"organization_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_org_member_org_id", columnList = "organization_id"),
                @Index(name = "idx_org_member_user_id", columnList = "user_id"),
                @Index(name = "idx_org_member_role", columnList = "role")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class OrganizationMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrganizationRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MemberStatus status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}

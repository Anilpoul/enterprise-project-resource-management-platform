package com.enterprise.platform.resource.entity;

import com.enterprise.platform.resource.enums.ResourceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resource_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_resource_profiles_org_user", columnNames = {"organization_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "job_title", nullable = false, length = 150)
    private String jobTitle;

    @Builder.Default
    @Column(name = "weekly_capacity_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal weeklyCapacityHours = new BigDecimal("40.00");

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Builder.Default
    @Column(name = "currency", length = 10)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ResourceStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "resourceProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceAllocation> allocations = new ArrayList<>();
}

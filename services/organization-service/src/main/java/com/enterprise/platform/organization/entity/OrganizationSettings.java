package com.enterprise.platform.organization.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organization_settings")
@Getter
@Setter
@NoArgsConstructor
public class OrganizationSettings extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, unique = true)
    private Organization organization;

    @Column(nullable = false, length = 50)
    private String timezone = "UTC";

    @Column(name = "date_format", nullable = false, length = 50)
    private String dateFormat = "YYYY-MM-DD";

    @Column(name = "allow_external_sharing", nullable = false)
    private Boolean allowExternalSharing = false;

    @Column(name = "mfa_required", nullable = false)
    private Boolean mfaRequired = false;

    @Column(name = "max_projects", nullable = false)
    private Integer maxProjects = 100;

    @Column(name = "max_users", nullable = false)
    private Integer maxUsers = 500;
}

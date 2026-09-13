package com.enterprise.platform.organization.repository;

import com.enterprise.platform.organization.entity.OrganizationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationSettingsRepository extends JpaRepository<OrganizationSettings, UUID> {

    Optional<OrganizationSettings> findByOrganizationId(UUID organizationId);
}

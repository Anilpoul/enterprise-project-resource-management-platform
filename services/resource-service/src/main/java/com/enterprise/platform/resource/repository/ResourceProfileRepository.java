package com.enterprise.platform.resource.repository;

import com.enterprise.platform.resource.entity.ResourceProfile;
import com.enterprise.platform.resource.enums.ResourceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceProfileRepository extends JpaRepository<ResourceProfile, UUID>, JpaSpecificationExecutor<ResourceProfile> {

    Optional<ResourceProfile> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Optional<ResourceProfile> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    Page<ResourceProfile> findByOrganizationId(UUID organizationId, Pageable pageable);

    Page<ResourceProfile> findByOrganizationIdAndStatus(UUID organizationId, ResourceStatus status, Pageable pageable);

    List<ResourceProfile> findByOrganizationId(UUID organizationId);

    List<ResourceProfile> findByOrganizationIdAndSkillsContainingIgnoreCase(UUID organizationId, String skill);
}

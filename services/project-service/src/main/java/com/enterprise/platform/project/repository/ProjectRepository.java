package com.enterprise.platform.project.repository;

import com.enterprise.platform.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    Optional<Project> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Optional<Project> findByOrganizationIdAndProjectKey(UUID organizationId, String projectKey);

    boolean existsByOrganizationIdAndProjectKey(UUID organizationId, String projectKey);

    boolean existsByOrganizationIdAndName(UUID organizationId, String name);

    Page<Project> findByOrganizationId(UUID organizationId, Pageable pageable);

    long countByOrganizationId(UUID organizationId);
}

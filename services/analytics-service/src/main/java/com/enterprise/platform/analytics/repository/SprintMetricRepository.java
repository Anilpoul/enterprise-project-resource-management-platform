package com.enterprise.platform.analytics.repository;

import com.enterprise.platform.analytics.entity.SprintMetricSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SprintMetricRepository extends JpaRepository<SprintMetricSnapshot, UUID>, JpaSpecificationExecutor<SprintMetricSnapshot> {

    Optional<SprintMetricSnapshot> findByOrganizationIdAndSprintId(UUID organizationId, UUID sprintId);

    List<SprintMetricSnapshot> findByOrganizationIdAndProjectId(UUID organizationId, UUID projectId);

    List<SprintMetricSnapshot> findByOrganizationId(UUID organizationId);
}

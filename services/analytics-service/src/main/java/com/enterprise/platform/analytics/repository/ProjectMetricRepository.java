package com.enterprise.platform.analytics.repository;

import com.enterprise.platform.analytics.entity.ProjectMetricSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMetricRepository extends JpaRepository<ProjectMetricSnapshot, UUID>, JpaSpecificationExecutor<ProjectMetricSnapshot> {

    Optional<ProjectMetricSnapshot> findByOrganizationIdAndProjectId(UUID organizationId, UUID projectId);

    List<ProjectMetricSnapshot> findByOrganizationId(UUID organizationId);

    long countByOrganizationId(UUID organizationId);

    @Query("SELECT COALESCE(SUM(p.totalTasks), 0) FROM ProjectMetricSnapshot p WHERE p.organizationId = :organizationId")
    Long sumTotalTasksByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("SELECT COALESCE(SUM(p.completedTasks), 0) FROM ProjectMetricSnapshot p WHERE p.organizationId = :organizationId")
    Long sumCompletedTasksByOrganizationId(@Param("organizationId") UUID organizationId);
}

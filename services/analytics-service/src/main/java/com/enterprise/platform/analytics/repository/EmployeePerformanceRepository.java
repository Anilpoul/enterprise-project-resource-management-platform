package com.enterprise.platform.analytics.repository;

import com.enterprise.platform.analytics.entity.EmployeePerformanceMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeePerformanceRepository extends JpaRepository<EmployeePerformanceMetric, UUID>, JpaSpecificationExecutor<EmployeePerformanceMetric> {

    Optional<EmployeePerformanceMetric> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    List<EmployeePerformanceMetric> findByOrganizationIdOrderByPerformanceScoreDesc(UUID organizationId);

    List<EmployeePerformanceMetric> findByOrganizationId(UUID organizationId);
}

package com.enterprise.platform.resource.repository;

import com.enterprise.platform.resource.entity.ResourceAllocation;
import com.enterprise.platform.resource.enums.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, UUID>, JpaSpecificationExecutor<ResourceAllocation> {

    Optional<ResourceAllocation> findByIdAndOrganizationId(UUID id, UUID organizationId);

    List<ResourceAllocation> findByResourceProfileIdAndOrganizationId(UUID resourceId, UUID organizationId);

    List<ResourceAllocation> findByProjectIdAndOrganizationId(UUID projectId, UUID organizationId);

    List<ResourceAllocation> findByOrganizationIdAndStatus(UUID organizationId, AllocationStatus status);

    List<ResourceAllocation> findByOrganizationId(UUID organizationId);

    @Query("SELECT COALESCE(SUM(ra.allocationPercentage), 0) FROM ResourceAllocation ra " +
           "WHERE ra.resourceProfile.id = :resourceId " +
           "AND ra.organizationId = :organizationId " +
           "AND ra.status IN :statuses " +
           "AND (:excludeAllocationId IS NULL OR ra.id != :excludeAllocationId)")
    BigDecimal sumAllocationPercentageByResource(
            @Param("resourceId") UUID resourceId,
            @Param("organizationId") UUID organizationId,
            @Param("statuses") Collection<AllocationStatus> statuses,
            @Param("excludeAllocationId") UUID excludeAllocationId
    );

    @Query("SELECT COALESCE(SUM(ra.allocationPercentage), 0) FROM ResourceAllocation ra " +
           "WHERE ra.resourceProfile.id = :resourceId " +
           "AND ra.organizationId = :organizationId " +
           "AND ra.status IN :statuses " +
           "AND ra.startDate <= :endDate AND ra.endDate >= :startDate " +
           "AND (:excludeAllocationId IS NULL OR ra.id != :excludeAllocationId)")
    BigDecimal sumOverlappingAllocationPercentage(
            @Param("resourceId") UUID resourceId,
            @Param("organizationId") UUID organizationId,
            @Param("statuses") Collection<AllocationStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeAllocationId") UUID excludeAllocationId
    );

    @Query("SELECT ra FROM ResourceAllocation ra " +
           "WHERE ra.resourceProfile.id = :resourceId " +
           "AND ra.organizationId = :organizationId " +
           "AND ra.status IN :statuses " +
           "AND ra.startDate <= :endDate AND ra.endDate >= :startDate")
    List<ResourceAllocation> findOverlappingAllocations(
            @Param("resourceId") UUID resourceId,
            @Param("organizationId") UUID organizationId,
            @Param("statuses") Collection<AllocationStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

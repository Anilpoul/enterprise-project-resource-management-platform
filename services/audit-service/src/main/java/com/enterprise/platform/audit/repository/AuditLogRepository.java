package com.enterprise.platform.audit.repository;

import com.enterprise.platform.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {

    Page<AuditLog> findByOrganizationIdAndEntityTypeAndEntityId(
            UUID organizationId, String entityType, String entityId, Pageable pageable);

    Optional<AuditLog> findByIdAndOrganizationId(UUID id, UUID organizationId);

    long countByOrganizationId(UUID organizationId);

    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.organizationId = :organizationId GROUP BY a.action")
    List<Object[]> countByActionGrouped(@Param("organizationId") UUID organizationId);

    @Query("SELECT a.entityType, COUNT(a) FROM AuditLog a WHERE a.organizationId = :organizationId GROUP BY a.entityType")
    List<Object[]> countByEntityTypeGrouped(@Param("organizationId") UUID organizationId);

    @Query("SELECT a.status, COUNT(a) FROM AuditLog a WHERE a.organizationId = :organizationId GROUP BY a.status")
    List<Object[]> countByStatusGrouped(@Param("organizationId") UUID organizationId);
}

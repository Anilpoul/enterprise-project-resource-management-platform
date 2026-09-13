package com.enterprise.platform.audit.service.impl;

import com.enterprise.platform.audit.context.TenantContext;
import com.enterprise.platform.audit.dto.request.AuditLogSearchCriteria;
import com.enterprise.platform.audit.dto.request.CreateAuditLogRequest;
import com.enterprise.platform.audit.dto.response.AuditLogResponse;
import com.enterprise.platform.audit.dto.response.AuditSummaryResponse;
import com.enterprise.platform.audit.dto.response.PagedResponse;
import com.enterprise.platform.audit.entity.AuditLog;
import com.enterprise.platform.audit.exception.BadRequestException;
import com.enterprise.platform.audit.exception.ResourceNotFoundException;
import com.enterprise.platform.audit.mapper.AuditLogMapper;
import com.enterprise.platform.audit.repository.AuditLogRepository;
import com.enterprise.platform.audit.service.AuditLogService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional
    public AuditLogResponse recordAuditLog(CreateAuditLogRequest request) {
        UUID orgId = request.getOrganizationId() != null ? request.getOrganizationId() : TenantContext.getOrganizationId();
        if (orgId == null) {
            throw new BadRequestException("Organization ID is required for recording audit log");
        }

        AuditLog auditLog = auditLogMapper.toEntity(request);
        auditLog.setOrganizationId(orgId);
        if (auditLog.getTimestamp() == null) {
            auditLog.setTimestamp(LocalDateTime.now());
        }
        if (auditLog.getCreatedAt() == null) {
            auditLog.setCreatedAt(LocalDateTime.now());
        }

        AuditLog saved = auditLogRepository.save(auditLog);
        log.info("Recorded audit log {} for org {} action {}", saved.getId(), orgId, saved.getAction());
        return auditLogMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void recordEvent(UUID organizationId, String entityType, String entityId, String action,
                            String performedBy, String ipAddress, String status, String details, LocalDateTime timestamp) {
        if (organizationId == null) {
            log.warn("Cannot record audit event with null organizationId for action {}", action);
            return;
        }

        AuditLog logEntry = AuditLog.builder()
                .organizationId(organizationId)
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .performedBy(performedBy)
                .ipAddress(ipAddress)
                .status(status != null ? status : "SUCCESS")
                .details(details)
                .timestamp(timestamp != null ? timestamp : LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(logEntry);
        log.debug("Recorded event: action={}, entityType={}, entityId={}", action, entityType, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> searchAuditLogs(AuditLogSearchCriteria criteria, Pageable pageable) {
        UUID orgId = validateOrganizationId();

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organizationId"), orgId));

            if (criteria != null) {
                if (criteria.getEntityType() != null && !criteria.getEntityType().isBlank()) {
                    predicates.add(cb.equal(cb.upper(root.get("entityType")), criteria.getEntityType().trim().toUpperCase()));
                }
                if (criteria.getEntityId() != null && !criteria.getEntityId().isBlank()) {
                    predicates.add(cb.equal(root.get("entityId"), criteria.getEntityId().trim()));
                }
                if (criteria.getAction() != null && !criteria.getAction().isBlank()) {
                    predicates.add(cb.equal(cb.upper(root.get("action")), criteria.getAction().trim().toUpperCase()));
                }
                if (criteria.getPerformedBy() != null && !criteria.getPerformedBy().isBlank()) {
                    predicates.add(cb.equal(root.get("performedBy"), criteria.getPerformedBy().trim()));
                }
                if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
                    predicates.add(cb.equal(cb.upper(root.get("status")), criteria.getStatus().trim().toUpperCase()));
                }
                if (criteria.getFromTimestamp() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), criteria.getFromTimestamp()));
                }
                if (criteria.getToTimestamp() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), criteria.getToTimestamp()));
                }
                if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
                    String pattern = "%" + criteria.getKeyword().trim().toLowerCase() + "%";
                    Predicate detailsLike = cb.like(cb.lower(root.get("details")), pattern);
                    Predicate actionLike = cb.like(cb.lower(root.get("action")), pattern);
                    predicates.add(cb.or(detailsLike, actionLike));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);
        List<AuditLogResponse> content = page.getContent().stream()
                .map(auditLogMapper::toResponse)
                .toList();

        return PagedResponse.<AuditLogResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLogById(UUID id) {
        UUID orgId = validateOrganizationId();
        AuditLog auditLog = auditLogRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found with ID: " + id));
        return auditLogMapper.toResponse(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getEntityAuditHistory(String entityType, String entityId, Pageable pageable) {
        UUID orgId = validateOrganizationId();
        Page<AuditLog> page = auditLogRepository.findByOrganizationIdAndEntityTypeAndEntityId(
                orgId, entityType, entityId, pageable);

        List<AuditLogResponse> content = page.getContent().stream()
                .map(auditLogMapper::toResponse)
                .toList();

        return PagedResponse.<AuditLogResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuditSummaryResponse getAuditSummary() {
        UUID orgId = validateOrganizationId();
        long total = auditLogRepository.countByOrganizationId(orgId);

        Map<String, Long> countByAction = convertGroupedResults(auditLogRepository.countByActionGrouped(orgId));
        Map<String, Long> countByEntityType = convertGroupedResults(auditLogRepository.countByEntityTypeGrouped(orgId));
        Map<String, Long> countByStatus = convertGroupedResults(auditLogRepository.countByStatusGrouped(orgId));

        return AuditSummaryResponse.builder()
                .organizationId(orgId)
                .totalAuditLogs(total)
                .countByAction(countByAction)
                .countByEntityType(countByEntityType)
                .countByStatus(countByStatus)
                .build();
    }

    private Map<String, Long> convertGroupedResults(List<Object[]> rows) {
        if (rows == null) {
            return Collections.emptyMap();
        }
        return rows.stream()
                .filter(row -> row != null && row.length >= 2 && row[0] != null)
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> ((Number) row[1]).longValue(),
                        (existing, replacement) -> existing
                ));
    }

    private UUID validateOrganizationId() {
        UUID orgId = TenantContext.getOrganizationId();
        if (orgId == null) {
            throw new BadRequestException("Missing X-Organization-Id header for tenant isolation");
        }
        return orgId;
    }
}

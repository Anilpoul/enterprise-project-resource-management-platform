package com.enterprise.platform.audit.service;

import com.enterprise.platform.audit.dto.request.AuditLogSearchCriteria;
import com.enterprise.platform.audit.dto.request.CreateAuditLogRequest;
import com.enterprise.platform.audit.dto.response.AuditLogResponse;
import com.enterprise.platform.audit.dto.response.AuditSummaryResponse;
import com.enterprise.platform.audit.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditLogService {

    AuditLogResponse recordAuditLog(CreateAuditLogRequest request);

    void recordEvent(UUID organizationId, String entityType, String entityId, String action,
                     String performedBy, String ipAddress, String status, String details, LocalDateTime timestamp);

    PagedResponse<AuditLogResponse> searchAuditLogs(AuditLogSearchCriteria criteria, Pageable pageable);

    AuditLogResponse getAuditLogById(UUID id);

    PagedResponse<AuditLogResponse> getEntityAuditHistory(String entityType, String entityId, Pageable pageable);

    AuditSummaryResponse getAuditSummary();
}

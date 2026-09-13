package com.enterprise.platform.audit.controller;

import com.enterprise.platform.audit.dto.request.AuditLogSearchCriteria;
import com.enterprise.platform.audit.dto.request.CreateAuditLogRequest;
import com.enterprise.platform.audit.dto.response.ApiResponse;
import com.enterprise.platform.audit.dto.response.AuditLogResponse;
import com.enterprise.platform.audit.dto.response.AuditSummaryResponse;
import com.enterprise.platform.audit.dto.response.PagedResponse;
import com.enterprise.platform.audit.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit & Compliance", description = "Endpoints for immutable activity audit trail, search, and compliance summaries")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "Record an audit log entry")
    public ResponseEntity<ApiResponse<AuditLogResponse>> recordAuditLog(
            @Valid @RequestBody CreateAuditLogRequest request) {
        AuditLogResponse response = auditLogService.recordAuditLog(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Audit log recorded successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Search and filter audit logs with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> searchAuditLogs(
            @ModelAttribute AuditLogSearchCriteria criteria,
            @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<AuditLogResponse> response = auditLogService.searchAuditLogs(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit log details by ID")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLogById(
            @PathVariable UUID id) {
        AuditLogResponse response = auditLogService.getAuditLogById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/entities/{entityType}/{entityId}")
    @Operation(summary = "Get complete chronological audit history timeline for a specific entity")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getEntityAuditHistory(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<AuditLogResponse> response = auditLogService.getEntityAuditHistory(entityType, entityId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get organization compliance and audit statistics summary")
    public ResponseEntity<ApiResponse<AuditSummaryResponse>> getAuditSummary() {
        AuditSummaryResponse response = auditLogService.getAuditSummary();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

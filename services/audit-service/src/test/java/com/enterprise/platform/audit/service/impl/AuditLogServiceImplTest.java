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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private UUID organizationId;
    private UUID logId;
    private AuditLog auditLog;
    private AuditLogResponse auditLogResponse;
    private CreateAuditLogRequest createRequest;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        logId = UUID.randomUUID();
        TenantContext.setOrganizationId(organizationId);

        auditLog = AuditLog.builder()
                .id(logId)
                .organizationId(organizationId)
                .entityType("TASK")
                .entityId("TASK-100")
                .action("TASK_CREATED")
                .performedBy("user-123")
                .ipAddress("127.0.0.1")
                .status("SUCCESS")
                .details("Created task 100")
                .timestamp(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        auditLogResponse = AuditLogResponse.builder()
                .id(logId)
                .organizationId(organizationId)
                .entityType("TASK")
                .entityId("TASK-100")
                .action("TASK_CREATED")
                .performedBy("user-123")
                .ipAddress("127.0.0.1")
                .status("SUCCESS")
                .details("Created task 100")
                .timestamp(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = CreateAuditLogRequest.builder()
                .organizationId(organizationId)
                .entityType("TASK")
                .entityId("TASK-100")
                .action("TASK_CREATED")
                .performedBy("user-123")
                .ipAddress("127.0.0.1")
                .status("SUCCESS")
                .details("Created task 100")
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("recordAuditLog should map, save and return response")
    void recordAuditLog_success() {
        when(auditLogMapper.toEntity(createRequest)).thenReturn(auditLog);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);
        when(auditLogMapper.toResponse(auditLog)).thenReturn(auditLogResponse);

        AuditLogResponse result = auditLogService.recordAuditLog(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(logId);
        assertThat(result.getAction()).isEqualTo("TASK_CREATED");
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("recordEvent should construct and save AuditLog directly")
    void recordEvent_success() {
        auditLogService.recordEvent(
                organizationId, "TASK", "TASK-101", "TASK_UPDATED",
                "user-456", "192.168.1.1", "SUCCESS", "Updated status", LocalDateTime.now());

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getOrganizationId()).isEqualTo(organizationId);
        assertThat(saved.getEntityType()).isEqualTo("TASK");
        assertThat(saved.getEntityId()).isEqualTo("TASK-101");
        assertThat(saved.getAction()).isEqualTo("TASK_UPDATED");
        assertThat(saved.getPerformedBy()).isEqualTo("user-456");
    }

    @Test
    @DisplayName("recordEvent with null organizationId should log warning and not save")
    void recordEvent_nullOrg_skipped() {
        auditLogService.recordEvent(
                null, "AUTH", null, "LOGIN", "user-1", null, "SUCCESS", "details", null);

        verify(auditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("searchAuditLogs should return paged responses matching criteria")
    void searchAuditLogs_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog), pageable, 1);

        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .entityType("TASK")
                .action("TASK_CREATED")
                .keyword("task")
                .build();

        when(auditLogRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(auditLogMapper.toResponse(auditLog)).thenReturn(auditLogResponse);

        PagedResponse<AuditLogResponse> result = auditLogService.searchAuditLogs(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAuditLogById should return audit log when found")
    void getAuditLogById_success() {
        when(auditLogRepository.findByIdAndOrganizationId(logId, organizationId))
                .thenReturn(Optional.of(auditLog));
        when(auditLogMapper.toResponse(auditLog)).thenReturn(auditLogResponse);

        AuditLogResponse result = auditLogService.getAuditLogById(logId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(logId);
    }

    @Test
    @DisplayName("getAuditLogById should throw ResourceNotFoundException when not found")
    void getAuditLogById_notFound() {
        when(auditLogRepository.findByIdAndOrganizationId(logId, organizationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditLogService.getAuditLogById(logId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Audit log not found with ID");
    }

    @Test
    @DisplayName("getEntityAuditHistory should return timeline logs for entity")
    void getEntityAuditHistory_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog), pageable, 1);

        when(auditLogRepository.findByOrganizationIdAndEntityTypeAndEntityId(organizationId, "TASK", "TASK-100", pageable))
                .thenReturn(page);
        when(auditLogMapper.toResponse(auditLog)).thenReturn(auditLogResponse);

        PagedResponse<AuditLogResponse> result = auditLogService.getEntityAuditHistory("TASK", "TASK-100", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(auditLogRepository).findByOrganizationIdAndEntityTypeAndEntityId(organizationId, "TASK", "TASK-100", pageable);
    }

    @Test
    @DisplayName("getAuditSummary should return aggregated statistics")
    void getAuditSummary_success() {
        when(auditLogRepository.countByOrganizationId(organizationId)).thenReturn(10L);

        List<Object[]> actions = List.<Object[]>of(new Object[]{"TASK_CREATED", 6L}, new Object[]{"PROJECT_UPDATED", 4L});
        List<Object[]> entityTypes = List.<Object[]>of(new Object[]{"TASK", 6L}, new Object[]{"PROJECT", 4L});
        List<Object[]> statuses = List.<Object[]>of(new Object[]{"SUCCESS", 10L});

        when(auditLogRepository.countByActionGrouped(organizationId)).thenReturn(actions);
        when(auditLogRepository.countByEntityTypeGrouped(organizationId)).thenReturn(entityTypes);
        when(auditLogRepository.countByStatusGrouped(organizationId)).thenReturn(statuses);

        AuditSummaryResponse summary = auditLogService.getAuditSummary();

        assertThat(summary).isNotNull();
        assertThat(summary.getOrganizationId()).isEqualTo(organizationId);
        assertThat(summary.getTotalAuditLogs()).isEqualTo(10L);
        assertThat(summary.getCountByAction()).containsEntry("TASK_CREATED", 6L);
        assertThat(summary.getCountByEntityType()).containsEntry("TASK", 6L);
        assertThat(summary.getCountByStatus()).containsEntry("SUCCESS", 10L);
    }

    @Test
    @DisplayName("should throw BadRequestException when organization header is missing")
    void missingOrganizationHeader_throwsBadRequest() {
        TenantContext.clear();

        assertThatThrownBy(() -> auditLogService.getAuditSummary())
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Missing X-Organization-Id header");
    }
}

package com.enterprise.platform.audit.controller;

import com.enterprise.platform.audit.dto.request.AuditLogSearchCriteria;
import com.enterprise.platform.audit.dto.request.CreateAuditLogRequest;
import com.enterprise.platform.audit.dto.response.AuditLogResponse;
import com.enterprise.platform.audit.dto.response.AuditSummaryResponse;
import com.enterprise.platform.audit.dto.response.PagedResponse;
import com.enterprise.platform.audit.exception.GlobalExceptionHandler;
import com.enterprise.platform.audit.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuditLogController auditLogController;

    private ObjectMapper objectMapper;
    private UUID logId;
    private UUID organizationId;
    private AuditLogResponse auditLogResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(auditLogController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        logId = UUID.randomUUID();
        organizationId = UUID.randomUUID();

        auditLogResponse = AuditLogResponse.builder()
                .id(logId)
                .organizationId(organizationId)
                .entityType("TASK")
                .entityId("TASK-100")
                .action("TASK_CREATED")
                .performedBy("user-123")
                .status("SUCCESS")
                .details("Created task 100")
                .timestamp(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/audit should record and return 201 Created")
    void recordAuditLog() throws Exception {
        CreateAuditLogRequest request = CreateAuditLogRequest.builder()
                .entityType("TASK")
                .entityId("TASK-100")
                .action("TASK_CREATED")
                .performedBy("user-123")
                .details("Created task 100")
                .build();

        when(auditLogService.recordAuditLog(any(CreateAuditLogRequest.class))).thenReturn(auditLogResponse);

        mockMvc.perform(post("/api/v1/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(logId.toString()))
                .andExpect(jsonPath("$.data.action").value("TASK_CREATED"));
    }

    @Test
    @DisplayName("GET /api/v1/audit should return 200 OK and paged logs")
    void searchAuditLogs() throws Exception {
        PagedResponse<AuditLogResponse> pagedResponse = PagedResponse.<AuditLogResponse>builder()
                .content(List.of(auditLogResponse))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(auditLogService.searchAuditLogs(any(AuditLogSearchCriteria.class), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/audit")
                        .param("entityType", "TASK")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].entityType").value("TASK"));
    }

    @Test
    @DisplayName("GET /api/v1/audit/{id} should return 200 OK and audit log")
    void getAuditLogById() throws Exception {
        when(auditLogService.getAuditLogById(logId)).thenReturn(auditLogResponse);

        mockMvc.perform(get("/api/v1/audit/{id}", logId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(logId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/audit/entities/{entityType}/{entityId} should return entity history")
    void getEntityAuditHistory() throws Exception {
        PagedResponse<AuditLogResponse> pagedResponse = PagedResponse.<AuditLogResponse>builder()
                .content(List.of(auditLogResponse))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(auditLogService.getEntityAuditHistory(eq("TASK"), eq("TASK-100"), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/audit/entities/TASK/TASK-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].entityId").value("TASK-100"));
    }

    @Test
    @DisplayName("GET /api/v1/audit/summary should return audit statistics")
    void getAuditSummary() throws Exception {
        AuditSummaryResponse summary = AuditSummaryResponse.builder()
                .organizationId(organizationId)
                .totalAuditLogs(25L)
                .countByAction(Map.of("TASK_CREATED", 15L, "PROJECT_UPDATED", 10L))
                .countByEntityType(Map.of("TASK", 15L, "PROJECT", 10L))
                .countByStatus(Map.of("SUCCESS", 25L))
                .build();

        when(auditLogService.getAuditSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/audit/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAuditLogs").value(25))
                .andExpect(jsonPath("$.data.countByAction.TASK_CREATED").value(15));
    }
}

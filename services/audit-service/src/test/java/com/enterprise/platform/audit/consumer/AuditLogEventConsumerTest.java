package com.enterprise.platform.audit.consumer;

import com.enterprise.platform.events.*;
import com.enterprise.platform.audit.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogEventConsumerTest {

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuditLogEventConsumer consumer;

    private UUID organizationId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("handleAuthEvent should record AUTH event")
    void handleAuthEvent() {
        AuthEvent event = AuthEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(AuthEventType.USER_REGISTERED)
                .userId(userId)
                .email("test@example.com")
                .timestamp(LocalDateTime.now())
                .details("Registered user")
                .build();

        consumer.handleAuthEvent(event);

        verify(auditLogService).recordEvent(
                isNull(), eq("AUTH"), eq(userId.toString()), eq("USER_REGISTERED"),
                eq(userId.toString()), isNull(), eq("SUCCESS"), eq("Registered user"), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("handleOrganizationEvent should record ORGANIZATION event")
    void handleOrganizationEvent() {
        OrganizationEvent event = OrganizationEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(OrganizationEventType.ORGANIZATION_CREATED)
                .organizationId(organizationId)
                .organizationName("Acme Corp")
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .build();

        consumer.handleOrganizationEvent(event);

        verify(auditLogService).recordEvent(
                eq(organizationId), eq("ORGANIZATION"), eq(organizationId.toString()), eq("ORGANIZATION_CREATED"),
                eq(userId.toString()), isNull(), eq("SUCCESS"), anyString(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("handleProjectEvent should record PROJECT event")
    void handleProjectEvent() {
        UUID projectId = UUID.randomUUID();
        ProjectEvent event = ProjectEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(ProjectEventType.PROJECT_CREATED)
                .organizationId(organizationId)
                .projectId(projectId)
                .projectName("Core Engine")
                .projectKey("ENG")
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .build();

        consumer.handleProjectEvent(event);

        verify(auditLogService).recordEvent(
                eq(organizationId), eq("PROJECT"), eq(projectId.toString()), eq("PROJECT_CREATED"),
                eq(userId.toString()), isNull(), eq("SUCCESS"), anyString(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("handleTaskEvent should record TASK event")
    void handleTaskEvent() {
        UUID taskId = UUID.randomUUID();
        TaskEvent event = TaskEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(TaskEventType.TASK_CREATED)
                .organizationId(organizationId)
                .taskId(taskId)
                .taskKey("ENG-101")
                .title("Implement Audit Service")
                .reporterId(userId)
                .timestamp(LocalDateTime.now())
                .build();

        consumer.handleTaskEvent(event);

        verify(auditLogService).recordEvent(
                eq(organizationId), eq("TASK"), eq(taskId.toString()), eq("TASK_CREATED"),
                eq(userId.toString()), isNull(), eq("SUCCESS"), anyString(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("handleSprintEvent should record SPRINT event")
    void handleSprintEvent() {
        UUID sprintId = UUID.randomUUID();
        SprintEvent event = SprintEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(SprintEventType.SPRINT_STARTED)
                .organizationId(organizationId)
                .sprintId(sprintId)
                .sprintName("Sprint 1")
                .status("ACTIVE")
                .timestamp(LocalDateTime.now())
                .build();

        consumer.handleSprintEvent(event);

        verify(auditLogService).recordEvent(
                eq(organizationId), eq("SPRINT"), eq(sprintId.toString()), eq("SPRINT_STARTED"),
                eq("SYSTEM"), isNull(), eq("SUCCESS"), anyString(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("handleResourceEvent should record RESOURCE event")
    void handleResourceEvent() {
        UUID resourceId = UUID.randomUUID();
        ResourceEvent event = ResourceEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(ResourceEventType.RESOURCE_ALLOCATED)
                .organizationId(organizationId)
                .resourceId(resourceId)
                .userId(userId)
                .allocationPercentage(100)
                .timestamp(LocalDateTime.now())
                .build();

        consumer.handleResourceEvent(event);

        verify(auditLogService).recordEvent(
                eq(organizationId), eq("RESOURCE"), eq(resourceId.toString()), eq("RESOURCE_ALLOCATED"),
                eq(userId.toString()), isNull(), eq("SUCCESS"), anyString(), any(LocalDateTime.class));
    }
}

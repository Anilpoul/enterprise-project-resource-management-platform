package com.enterprise.platform.notification.consumer;

import com.enterprise.platform.events.ResourceEvent;
import com.enterprise.platform.events.ResourceEventType;
import com.enterprise.platform.events.SprintEvent;
import com.enterprise.platform.events.SprintEventType;
import com.enterprise.platform.events.TaskEvent;
import com.enterprise.platform.events.TaskEventType;
import com.enterprise.platform.notification.constants.NotificationType;
import com.enterprise.platform.notification.dto.request.CreateNotificationRequest;
import com.enterprise.platform.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventConsumer eventConsumer;

    private UUID organizationId;
    private UUID userId;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();
    }

    @Test
    @DisplayName("handleTaskEvent should create notification when task is assigned")
    void handleTaskEvent_taskAssigned() {
        TaskEvent event = TaskEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(TaskEventType.TASK_ASSIGNED)
                .organizationId(organizationId)
                .taskId(taskId)
                .taskKey("PROJ-101")
                .title("Implement Authentication")
                .assigneeId(userId)
                .timestamp(LocalDateTime.now())
                .build();

        eventConsumer.handleTaskEvent(event);

        ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationService).createInternalNotification(eq(organizationId), captor.capture());

        CreateNotificationRequest req = captor.getValue();
        assertThat(req.getRecipientId()).isEqualTo(userId);
        assertThat(req.getNotificationType()).isEqualTo(NotificationType.TASK_ASSIGNED);
        assertThat(req.getTitle()).contains("PROJ-101");
    }

    @Test
    @DisplayName("handleTaskEvent should create notification when task status changes")
    void handleTaskEvent_statusChanged() {
        TaskEvent event = TaskEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(TaskEventType.TASK_STATUS_CHANGED)
                .organizationId(organizationId)
                .taskId(taskId)
                .taskKey("PROJ-102")
                .title("Fix bug")
                .status("DONE")
                .assigneeId(userId)
                .timestamp(LocalDateTime.now())
                .build();

        eventConsumer.handleTaskEvent(event);

        ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationService).createInternalNotification(eq(organizationId), captor.capture());

        CreateNotificationRequest req = captor.getValue();
        assertThat(req.getNotificationType()).isEqualTo(NotificationType.TASK_STATUS_CHANGED);
        assertThat(req.getMessage()).contains("DONE");
    }

    @Test
    @DisplayName("handleTaskEvent should skip when assignee is null")
    void handleTaskEvent_assigneeNull_skipped() {
        TaskEvent event = TaskEvent.builder()
                .eventType(TaskEventType.TASK_ASSIGNED)
                .organizationId(organizationId)
                .taskId(taskId)
                .assigneeId(null)
                .build();

        eventConsumer.handleTaskEvent(event);

        verify(notificationService, never()).createInternalNotification(any(), any());
    }

    @Test
    @DisplayName("handleResourceEvent should notify when resource is allocated")
    void handleResourceEvent_allocated() {
        ResourceEvent event = ResourceEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(ResourceEventType.RESOURCE_ALLOCATED)
                .organizationId(organizationId)
                .userId(userId)
                .resourceId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .allocationPercentage(80)
                .timestamp(LocalDateTime.now())
                .build();

        eventConsumer.handleResourceEvent(event);

        ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationService, times(1)).createInternalNotification(eq(organizationId), captor.capture());

        CreateNotificationRequest req = captor.getValue();
        assertThat(req.getNotificationType()).isEqualTo(NotificationType.RESOURCE_ALLOCATED);
        assertThat(req.getMessage()).contains("80%");
    }

    @Test
    @DisplayName("handleResourceEvent should send over-allocation warning when percentage > 100")
    void handleResourceEvent_overallocated() {
        ResourceEvent event = ResourceEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(ResourceEventType.RESOURCE_ALLOCATED)
                .organizationId(organizationId)
                .userId(userId)
                .resourceId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .allocationPercentage(120)
                .timestamp(LocalDateTime.now())
                .build();

        eventConsumer.handleResourceEvent(event);

        // Expect 2 calls: 1 allocation update + 1 overallocation warning
        verify(notificationService, times(2)).createInternalNotification(eq(organizationId), any(CreateNotificationRequest.class));
    }

    @Test
    @DisplayName("handleSprintEvent should log gracefully")
    void handleSprintEvent_graceful() {
        SprintEvent event = SprintEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(SprintEventType.SPRINT_STARTED)
                .organizationId(organizationId)
                .sprintId(UUID.randomUUID())
                .sprintName("Sprint 1")
                .build();

        eventConsumer.handleSprintEvent(event);
        // sprint event handled without exception
    }
}

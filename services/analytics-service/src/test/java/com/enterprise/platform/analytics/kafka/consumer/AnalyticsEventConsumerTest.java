package com.enterprise.platform.analytics.kafka.consumer;

import com.enterprise.platform.analytics.entity.EmployeePerformanceMetric;
import com.enterprise.platform.analytics.entity.ProjectMetricSnapshot;
import com.enterprise.platform.analytics.entity.SprintMetricSnapshot;
import com.enterprise.platform.analytics.repository.EmployeePerformanceRepository;
import com.enterprise.platform.analytics.repository.ProjectMetricRepository;
import com.enterprise.platform.analytics.repository.SprintMetricRepository;
import com.enterprise.platform.events.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventConsumerTest {

    @Mock
    private ProjectMetricRepository projectMetricRepository;

    @Mock
    private SprintMetricRepository sprintMetricRepository;

    @Mock
    private EmployeePerformanceRepository employeePerformanceRepository;

    @InjectMocks
    private AnalyticsEventConsumer eventConsumer;

    private UUID organizationId;
    private UUID projectId;
    private UUID userId;
    private UUID taskId;
    private UUID sprintId;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        sprintId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should increment totalTasks on TASK_CREATED event")
    void testConsumeTaskEvent_TaskCreated() {
        TaskEvent event = TaskEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(TaskEventType.TASK_CREATED)
                .organizationId(organizationId)
                .projectId(projectId)
                .taskId(taskId)
                .build();

        when(projectMetricRepository.findByOrganizationIdAndProjectId(organizationId, projectId))
                .thenReturn(Optional.empty());

        eventConsumer.consumeTaskEvent(event);

        ArgumentCaptor<ProjectMetricSnapshot> captor = ArgumentCaptor.forClass(ProjectMetricSnapshot.class);
        verify(projectMetricRepository).save(captor.capture());

        ProjectMetricSnapshot saved = captor.getValue();
        assertThat(saved.getTotalTasks()).isEqualTo(1);
        assertThat(saved.getCompletedTasks()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should update completed tasks and employee metrics on TASK_STATUS_CHANGED to DONE")
    void testConsumeTaskEvent_TaskStatusChangedToDone() {
        TaskEvent event = TaskEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(TaskEventType.TASK_STATUS_CHANGED)
                .organizationId(organizationId)
                .projectId(projectId)
                .taskId(taskId)
                .status("DONE")
                .assigneeId(userId)
                .build();

        ProjectMetricSnapshot existingSnapshot = ProjectMetricSnapshot.builder()
                .organizationId(organizationId)
                .projectId(projectId)
                .totalTasks(4)
                .completedTasks(1)
                .build();

        when(projectMetricRepository.findByOrganizationIdAndProjectId(organizationId, projectId))
                .thenReturn(Optional.of(existingSnapshot));
        when(employeePerformanceRepository.findByOrganizationIdAndUserId(organizationId, userId))
                .thenReturn(Optional.empty());

        eventConsumer.consumeTaskEvent(event);

        verify(projectMetricRepository).save(existingSnapshot);
        assertThat(existingSnapshot.getCompletedTasks()).isEqualTo(2);
        assertThat(existingSnapshot.getCompletionRate()).isEqualByComparingTo(new BigDecimal("50.00"));

        verify(employeePerformanceRepository).save(any(EmployeePerformanceMetric.class));
    }

    @Test
    @DisplayName("Should skip event when organizationId or projectId is missing")
    void testConsumeTaskEvent_MissingOrgId_Ignored() {
        TaskEvent event = TaskEvent.builder()
                .eventType(TaskEventType.TASK_CREATED)
                .taskId(taskId)
                .build();

        eventConsumer.consumeTaskEvent(event);

        verify(projectMetricRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should record sprint metrics on SPRINT_COMPLETED event")
    void testConsumeSprintEvent_SprintCompleted() {
        SprintEvent event = SprintEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(SprintEventType.SPRINT_COMPLETED)
                .organizationId(organizationId)
                .projectId(projectId)
                .sprintId(sprintId)
                .sprintName("Sprint 1")
                .timestamp(LocalDateTime.now())
                .build();

        when(sprintMetricRepository.findByOrganizationIdAndSprintId(organizationId, sprintId))
                .thenReturn(Optional.empty());

        eventConsumer.consumeSprintEvent(event);

        ArgumentCaptor<SprintMetricSnapshot> captor = ArgumentCaptor.forClass(SprintMetricSnapshot.class);
        verify(sprintMetricRepository).save(captor.capture());

        SprintMetricSnapshot saved = captor.getValue();
        assertThat(saved.getSprintName()).isEqualTo("Sprint 1");
        assertThat(saved.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("Should initialize project metric snapshot on PROJECT_CREATED event")
    void testConsumeProjectEvent_ProjectCreated() {
        ProjectEvent event = ProjectEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(ProjectEventType.PROJECT_CREATED)
                .organizationId(organizationId)
                .projectId(projectId)
                .projectName("Core Platform")
                .build();

        when(projectMetricRepository.findByOrganizationIdAndProjectId(organizationId, projectId))
                .thenReturn(Optional.empty());

        eventConsumer.consumeProjectEvent(event);

        ArgumentCaptor<ProjectMetricSnapshot> captor = ArgumentCaptor.forClass(ProjectMetricSnapshot.class);
        verify(projectMetricRepository).save(captor.capture());

        ProjectMetricSnapshot saved = captor.getValue();
        assertThat(saved.getProjectId()).isEqualTo(projectId);
        assertThat(saved.getTotalTasks()).isEqualTo(0);
    }
}

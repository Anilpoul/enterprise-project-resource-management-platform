package com.enterprise.platform.analytics.kafka.consumer;

import com.enterprise.platform.analytics.entity.EmployeePerformanceMetric;
import com.enterprise.platform.analytics.entity.ProjectMetricSnapshot;
import com.enterprise.platform.analytics.entity.SprintMetricSnapshot;
import com.enterprise.platform.analytics.enums.PerformanceRating;
import com.enterprise.platform.analytics.enums.ProjectHealthStatus;
import com.enterprise.platform.analytics.repository.EmployeePerformanceRepository;
import com.enterprise.platform.analytics.repository.ProjectMetricRepository;
import com.enterprise.platform.analytics.repository.SprintMetricRepository;
import com.enterprise.platform.events.ProjectEvent;
import com.enterprise.platform.events.ProjectEventType;
import com.enterprise.platform.events.SprintEvent;
import com.enterprise.platform.events.SprintEventType;
import com.enterprise.platform.events.TaskEvent;
import com.enterprise.platform.events.TaskEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsEventConsumer {

    private final ProjectMetricRepository projectMetricRepository;
    private final SprintMetricRepository sprintMetricRepository;
    private final EmployeePerformanceRepository employeePerformanceRepository;

    @KafkaListener(topics = "${kafka.topics.task-events:task-events}", groupId = "analytics-service-group")
    @Transactional
    public void consumeTaskEvent(TaskEvent event) {
        log.info("Analytics consumer received TaskEvent: {} for task: {}", event.getEventType(), event.getTaskId());
        if (event.getOrganizationId() == null || event.getProjectId() == null) {
            log.warn("Skipping TaskEvent due to missing organizationId or projectId");
            return;
        }

        ProjectMetricSnapshot projectSnapshot = projectMetricRepository
                .findByOrganizationIdAndProjectId(event.getOrganizationId(), event.getProjectId())
                .orElseGet(() -> ProjectMetricSnapshot.builder()
                        .organizationId(event.getOrganizationId())
                        .projectId(event.getProjectId())
                        .totalTasks(0)
                        .completedTasks(0)
                        .inProgressTasks(0)
                        .blockedTasks(0)
                        .overdueTasks(0)
                        .completionRate(BigDecimal.ZERO)
                        .healthStatus(ProjectHealthStatus.ON_TRACK)
                        .build());

        TaskEventType type = event.getEventType();
        if (type == TaskEventType.TASK_CREATED) {
            projectSnapshot.setTotalTasks(projectSnapshot.getTotalTasks() + 1);
        } else if (type == TaskEventType.TASK_ASSIGNED && event.getAssigneeId() != null) {
            handleEmployeeTaskAssigned(event);
        } else if (type == TaskEventType.TASK_STATUS_CHANGED && event.getStatus() != null) {
            if ("DONE".equalsIgnoreCase(event.getStatus()) || "COMPLETED".equalsIgnoreCase(event.getStatus())) {
                projectSnapshot.setCompletedTasks(projectSnapshot.getCompletedTasks() + 1);
                if (event.getAssigneeId() != null) {
                    handleEmployeeTaskCompleted(event);
                }
            } else if ("IN_PROGRESS".equalsIgnoreCase(event.getStatus())) {
                projectSnapshot.setInProgressTasks(projectSnapshot.getInProgressTasks() + 1);
            } else if ("BLOCKED".equalsIgnoreCase(event.getStatus())) {
                projectSnapshot.setBlockedTasks(projectSnapshot.getBlockedTasks() + 1);
            }
        }

        // Recalculate project completion rate
        if (projectSnapshot.getTotalTasks() > 0) {
            BigDecimal rate = new BigDecimal(projectSnapshot.getCompletedTasks())
                    .multiply(new BigDecimal("100.00"))
                    .divide(new BigDecimal(projectSnapshot.getTotalTasks()), 2, RoundingMode.HALF_UP);
            projectSnapshot.setCompletionRate(rate);

            if (rate.compareTo(new BigDecimal("75.00")) >= 0) {
                projectSnapshot.setHealthStatus(ProjectHealthStatus.ON_TRACK);
            } else if (rate.compareTo(new BigDecimal("40.00")) >= 0) {
                projectSnapshot.setHealthStatus(ProjectHealthStatus.AT_RISK);
            } else if (projectSnapshot.getTotalTasks() >= 5) {
                projectSnapshot.setHealthStatus(ProjectHealthStatus.CRITICAL);
            }
        }

        projectMetricRepository.save(projectSnapshot);
    }

    private void handleEmployeeTaskAssigned(TaskEvent event) {
        EmployeePerformanceMetric metric = employeePerformanceRepository
                .findByOrganizationIdAndUserId(event.getOrganizationId(), event.getAssigneeId())
                .orElseGet(() -> EmployeePerformanceMetric.builder()
                        .organizationId(event.getOrganizationId())
                        .userId(event.getAssigneeId())
                        .tasksAssigned(0)
                        .tasksCompleted(0)
                        .tasksOverdue(0)
                        .storyPointsDelivered(0)
                        .onTimeCompletionRate(new BigDecimal("100.00"))
                        .performanceScore(new BigDecimal("100.00"))
                        .performanceRating(PerformanceRating.EXCELLENT)
                        .build());

        metric.setTasksAssigned(metric.getTasksAssigned() + 1);
        employeePerformanceRepository.save(metric);
    }

    private void handleEmployeeTaskCompleted(TaskEvent event) {
        EmployeePerformanceMetric metric = employeePerformanceRepository
                .findByOrganizationIdAndUserId(event.getOrganizationId(), event.getAssigneeId())
                .orElseGet(() -> EmployeePerformanceMetric.builder()
                        .organizationId(event.getOrganizationId())
                        .userId(event.getAssigneeId())
                        .tasksAssigned(1)
                        .tasksCompleted(0)
                        .tasksOverdue(0)
                        .storyPointsDelivered(0)
                        .onTimeCompletionRate(new BigDecimal("100.00"))
                        .performanceScore(new BigDecimal("100.00"))
                        .performanceRating(PerformanceRating.EXCELLENT)
                        .build());

        metric.setTasksCompleted(metric.getTasksCompleted() + 1);
        metric.setStoryPointsDelivered(metric.getStoryPointsDelivered() + 3); // Standard default points per task if not specified

        // Calculate performance score
        if (metric.getTasksAssigned() > 0) {
            BigDecimal completionRatio = new BigDecimal(metric.getTasksCompleted())
                    .multiply(new BigDecimal("100.00"))
                    .divide(new BigDecimal(metric.getTasksAssigned()), 2, RoundingMode.HALF_UP);
            if (completionRatio.compareTo(new BigDecimal("100.00")) > 0) {
                completionRatio = new BigDecimal("100.00");
            }
            metric.setPerformanceScore(completionRatio);

            if (completionRatio.compareTo(new BigDecimal("85.00")) >= 0) {
                metric.setPerformanceRating(PerformanceRating.EXCELLENT);
            } else if (completionRatio.compareTo(new BigDecimal("70.00")) >= 0) {
                metric.setPerformanceRating(PerformanceRating.GOOD);
            } else if (completionRatio.compareTo(new BigDecimal("50.00")) >= 0) {
                metric.setPerformanceRating(PerformanceRating.AVERAGE);
            } else {
                metric.setPerformanceRating(PerformanceRating.NEEDS_IMPROVEMENT);
            }
        }

        employeePerformanceRepository.save(metric);
    }

    @KafkaListener(topics = "${kafka.topics.sprint-events:sprint-events}", groupId = "analytics-service-group")
    @Transactional
    public void consumeSprintEvent(SprintEvent event) {
        log.info("Analytics consumer received SprintEvent: {} for sprint: {}", event.getEventType(), event.getSprintId());
        if (event.getOrganizationId() == null || event.getSprintId() == null) {
            log.warn("Skipping SprintEvent due to missing organizationId or sprintId");
            return;
        }

        if (event.getEventType() == SprintEventType.SPRINT_COMPLETED) {
            SprintMetricSnapshot sprintSnapshot = sprintMetricRepository
                    .findByOrganizationIdAndSprintId(event.getOrganizationId(), event.getSprintId())
                    .orElseGet(() -> SprintMetricSnapshot.builder()
                            .organizationId(event.getOrganizationId())
                            .projectId(event.getProjectId())
                            .sprintId(event.getSprintId())
                            .sprintName(event.getSprintName())
                            .status("COMPLETED")
                            .committedStoryPoints(20)
                            .completedStoryPoints(20)
                            .velocity(new BigDecimal("20.00"))
                            .completionRate(new BigDecimal("100.00"))
                            .spilloverTasksCount(0)
                            .build());

            sprintSnapshot.setStatus("COMPLETED");
            sprintSnapshot.setSprintName(event.getSprintName());
            sprintMetricRepository.save(sprintSnapshot);
        }
    }

    @KafkaListener(topics = "${kafka.topics.project-events:project-events}", groupId = "analytics-service-group")
    @Transactional
    public void consumeProjectEvent(ProjectEvent event) {
        log.info("Analytics consumer received ProjectEvent: {} for project: {}", event.getEventType(), event.getProjectId());
        if (event.getOrganizationId() == null || event.getProjectId() == null) {
            log.warn("Skipping ProjectEvent due to missing organizationId or projectId");
            return;
        }

        if (event.getEventType() == ProjectEventType.PROJECT_CREATED) {
            if (projectMetricRepository.findByOrganizationIdAndProjectId(event.getOrganizationId(), event.getProjectId()).isEmpty()) {
                ProjectMetricSnapshot snapshot = ProjectMetricSnapshot.builder()
                        .organizationId(event.getOrganizationId())
                        .projectId(event.getProjectId())
                        .totalTasks(0)
                        .completedTasks(0)
                        .inProgressTasks(0)
                        .blockedTasks(0)
                        .overdueTasks(0)
                        .completionRate(BigDecimal.ZERO)
                        .healthStatus(ProjectHealthStatus.ON_TRACK)
                        .build();
                projectMetricRepository.save(snapshot);
            }
        }
    }
}

package com.enterprise.platform.notification.consumer;

import com.enterprise.platform.events.ResourceEvent;
import com.enterprise.platform.events.ResourceEventType;
import com.enterprise.platform.events.SprintEvent;
import com.enterprise.platform.events.SprintEventType;
import com.enterprise.platform.events.TaskEvent;
import com.enterprise.platform.events.TaskEventType;
import com.enterprise.platform.notification.constants.NotificationChannel;
import com.enterprise.platform.notification.constants.NotificationType;
import com.enterprise.platform.notification.dto.request.CreateNotificationRequest;
import com.enterprise.platform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topics.task-events:task-events}", groupId = "${spring.kafka.consumer.group-id:notification-service-group}")
    public void handleTaskEvent(TaskEvent event) {
        if (event == null || event.getOrganizationId() == null) {
            return;
        }

        log.info("Received TaskEvent: type={}, taskId={}, assigneeId={}",
                event.getEventType(), event.getTaskId(), event.getAssigneeId());

        if (event.getAssigneeId() == null) {
            return;
        }

        TaskEventType type = event.getEventType();
        if (type == TaskEventType.TASK_ASSIGNED) {
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .recipientId(event.getAssigneeId())
                    .senderId(event.getReporterId())
                    .title("Task Assigned: " + (event.getTaskKey() != null ? event.getTaskKey() : "Task"))
                    .message("You have been assigned to task: " + event.getTitle())
                    .notificationType(NotificationType.TASK_ASSIGNED)
                    .referenceId(event.getTaskId())
                    .referenceType("TASK")
                    .channel(NotificationChannel.IN_APP)
                    .build();
            notificationService.createInternalNotification(event.getOrganizationId(), request);
        } else if (type == TaskEventType.TASK_STATUS_CHANGED) {
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .recipientId(event.getAssigneeId())
                    .senderId(event.getReporterId())
                    .title("Task Status Updated: " + (event.getTaskKey() != null ? event.getTaskKey() : "Task"))
                    .message("Task " + (event.getTaskKey() != null ? event.getTaskKey() : "") + " status changed to: " + event.getStatus())
                    .notificationType(NotificationType.TASK_STATUS_CHANGED)
                    .referenceId(event.getTaskId())
                    .referenceType("TASK")
                    .channel(NotificationChannel.IN_APP)
                    .build();
            notificationService.createInternalNotification(event.getOrganizationId(), request);
        } else if (type == TaskEventType.TASK_COMMENT_ADDED) {
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .recipientId(event.getAssigneeId())
                    .senderId(event.getReporterId())
                    .title("New Comment: " + (event.getTaskKey() != null ? event.getTaskKey() : "Task"))
                    .message("A new comment was added to your task: " + event.getTitle())
                    .notificationType(NotificationType.TASK_COMMENT_ADDED)
                    .referenceId(event.getTaskId())
                    .referenceType("TASK")
                    .channel(NotificationChannel.IN_APP)
                    .build();
            notificationService.createInternalNotification(event.getOrganizationId(), request);
        }
    }

    @KafkaListener(topics = "${kafka.topics.sprint-events:sprint-events}", groupId = "${spring.kafka.consumer.group-id:notification-service-group}")
    public void handleSprintEvent(SprintEvent event) {
        if (event == null || event.getOrganizationId() == null) {
            return;
        }

        log.info("Received SprintEvent: type={}, sprintId={}, name={}",
                event.getEventType(), event.getSprintId(), event.getSprintName());
    }

    @KafkaListener(topics = "${kafka.topics.resource-events:resource-events}", groupId = "${spring.kafka.consumer.group-id:notification-service-group}")
    public void handleResourceEvent(ResourceEvent event) {
        if (event == null || event.getOrganizationId() == null || event.getUserId() == null) {
            return;
        }

        log.info("Received ResourceEvent: type={}, userId={}, allocationPercentage={}",
                event.getEventType(), event.getUserId(), event.getAllocationPercentage());

        ResourceEventType type = event.getEventType();
        if (type == ResourceEventType.RESOURCE_ALLOCATED || type == ResourceEventType.ALLOCATION_UPDATED) {
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .recipientId(event.getUserId())
                    .title("Project Allocation Updated")
                    .message("Your allocation has been updated to " + event.getAllocationPercentage() + "% on project " + event.getProjectId())
                    .notificationType(NotificationType.RESOURCE_ALLOCATED)
                    .referenceId(event.getAllocationId() != null ? event.getAllocationId() : event.getResourceId())
                    .referenceType("RESOURCE_ALLOCATION")
                    .channel(NotificationChannel.IN_APP)
                    .build();
            notificationService.createInternalNotification(event.getOrganizationId(), request);

            if (event.getAllocationPercentage() != null && event.getAllocationPercentage() > 100) {
                CreateNotificationRequest alertRequest = CreateNotificationRequest.builder()
                        .recipientId(event.getUserId())
                        .title("Resource Overallocated Warning")
                        .message("Your total allocation has reached " + event.getAllocationPercentage() + "%, exceeding 100% capacity.")
                        .notificationType(NotificationType.RESOURCE_OVERALLOCATED)
                        .referenceId(event.getResourceId())
                        .referenceType("RESOURCE")
                        .channel(NotificationChannel.IN_APP)
                        .build();
                notificationService.createInternalNotification(event.getOrganizationId(), alertRequest);
            }
        }
    }
}

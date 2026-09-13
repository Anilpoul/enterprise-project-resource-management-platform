package com.enterprise.platform.audit.consumer;

import com.enterprise.platform.events.*;
import com.enterprise.platform.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogEventConsumer {

    private final AuditLogService auditLogService;

    @KafkaListener(topics = "${kafka.topics.auth-events:auth-events}", groupId = "${spring.kafka.consumer.group-id:audit-service-group}")
    public void handleAuthEvent(AuthEvent event) {
        if (event == null) return;
        log.info("Audit consumer received AuthEvent: type={}, userId={}", event.getEventType(), event.getUserId());
        auditLogService.recordEvent(
                null, // Auth events may be global or before org context
                "AUTH",
                event.getUserId() != null ? event.getUserId().toString() : null,
                event.getEventType() != null ? event.getEventType().name() : "AUTH_EVENT",
                event.getUserId() != null ? event.getUserId().toString() : "ANONYMOUS",
                null,
                "SUCCESS",
                event.getDetails() != null ? event.getDetails() : ("Email: " + event.getEmail()),
                event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now()
        );
    }

    @KafkaListener(topics = "${kafka.topics.organization-events:org-events}", groupId = "${spring.kafka.consumer.group-id:audit-service-group}")
    public void handleOrganizationEvent(OrganizationEvent event) {
        if (event == null || event.getOrganizationId() == null) return;
        log.info("Audit consumer received OrgEvent: type={}, orgId={}", event.getEventType(), event.getOrganizationId());
        auditLogService.recordEvent(
                event.getOrganizationId(),
                "ORGANIZATION",
                event.getOrganizationId().toString(),
                event.getEventType() != null ? event.getEventType().name() : "ORGANIZATION_EVENT",
                event.getUserId() != null ? event.getUserId().toString() : "SYSTEM",
                null,
                "SUCCESS",
                event.getDetails() != null ? event.getDetails() : ("Org Name: " + event.getOrganizationName()),
                event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now()
        );
    }

    @KafkaListener(topics = "${kafka.topics.project-events:project-events}", groupId = "${spring.kafka.consumer.group-id:audit-service-group}")
    public void handleProjectEvent(ProjectEvent event) {
        if (event == null || event.getOrganizationId() == null) return;
        log.info("Audit consumer received ProjectEvent: type={}, projectId={}", event.getEventType(), event.getProjectId());
        auditLogService.recordEvent(
                event.getOrganizationId(),
                "PROJECT",
                event.getProjectId() != null ? event.getProjectId().toString() : null,
                event.getEventType() != null ? event.getEventType().name() : "PROJECT_EVENT",
                event.getUserId() != null ? event.getUserId().toString() : "SYSTEM",
                null,
                "SUCCESS",
                event.getDetails() != null ? event.getDetails() : ("Project Key: " + event.getProjectKey()),
                event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now()
        );
    }

    @KafkaListener(topics = "${kafka.topics.task-events:task-events}", groupId = "${spring.kafka.consumer.group-id:audit-service-group}")
    public void handleTaskEvent(TaskEvent event) {
        if (event == null || event.getOrganizationId() == null) return;
        log.info("Audit consumer received TaskEvent: type={}, taskId={}", event.getEventType(), event.getTaskId());
        auditLogService.recordEvent(
                event.getOrganizationId(),
                "TASK",
                event.getTaskId() != null ? event.getTaskId().toString() : null,
                event.getEventType() != null ? event.getEventType().name() : "TASK_EVENT",
                event.getReporterId() != null ? event.getReporterId().toString() : (event.getAssigneeId() != null ? event.getAssigneeId().toString() : "SYSTEM"),
                null,
                "SUCCESS",
                "TaskKey: " + event.getTaskKey() + ", Title: " + event.getTitle() + ", Status: " + event.getStatus(),
                event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now()
        );
    }

    @KafkaListener(topics = "${kafka.topics.sprint-events:sprint-events}", groupId = "${spring.kafka.consumer.group-id:audit-service-group}")
    public void handleSprintEvent(SprintEvent event) {
        if (event == null || event.getOrganizationId() == null) return;
        log.info("Audit consumer received SprintEvent: type={}, sprintId={}", event.getEventType(), event.getSprintId());
        auditLogService.recordEvent(
                event.getOrganizationId(),
                "SPRINT",
                event.getSprintId() != null ? event.getSprintId().toString() : null,
                event.getEventType() != null ? event.getEventType().name() : "SPRINT_EVENT",
                "SYSTEM",
                null,
                "SUCCESS",
                "SprintName: " + event.getSprintName() + ", Status: " + event.getStatus(),
                event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now()
        );
    }

    @KafkaListener(topics = "${kafka.topics.resource-events:resource-events}", groupId = "${spring.kafka.consumer.group-id:audit-service-group}")
    public void handleResourceEvent(ResourceEvent event) {
        if (event == null || event.getOrganizationId() == null) return;
        log.info("Audit consumer received ResourceEvent: type={}, resourceId={}", event.getEventType(), event.getResourceId());
        auditLogService.recordEvent(
                event.getOrganizationId(),
                "RESOURCE",
                event.getResourceId() != null ? event.getResourceId().toString() : null,
                event.getEventType() != null ? event.getEventType().name() : "RESOURCE_EVENT",
                event.getUserId() != null ? event.getUserId().toString() : "SYSTEM",
                null,
                "SUCCESS",
                "Allocation: " + event.getAllocationPercentage() + "%, Project: " + event.getProjectId(),
                event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now()
        );
    }
}

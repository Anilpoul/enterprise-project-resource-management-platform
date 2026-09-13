package com.enterprise.platform.task.service.impl;

import com.enterprise.platform.events.TaskEvent;
import com.enterprise.platform.events.TaskEventType;
import com.enterprise.platform.task.constants.enums.TaskPriority;
import com.enterprise.platform.task.constants.enums.TaskStatus;
import com.enterprise.platform.task.context.TenantContext;
import com.enterprise.platform.task.dto.request.CreateTaskRequest;
import com.enterprise.platform.task.dto.request.TaskSearchCriteria;
import com.enterprise.platform.task.dto.request.UpdateTaskRequest;
import com.enterprise.platform.task.dto.response.PagedResponse;
import com.enterprise.platform.task.dto.response.TaskResponse;
import com.enterprise.platform.task.entity.Task;
import com.enterprise.platform.task.exception.BadRequestException;
import com.enterprise.platform.task.exception.ResourceNotFoundException;
import com.enterprise.platform.task.kafka.producer.TaskEventProducer;
import com.enterprise.platform.task.mapper.TaskMapper;
import com.enterprise.platform.task.repository.TaskRepository;
import com.enterprise.platform.task.service.TaskSequenceService;
import com.enterprise.platform.task.service.TaskService;
import com.enterprise.platform.task.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskSequenceService sequenceService;
    private final TaskMapper taskMapper;
    private final TaskEventProducer eventProducer;

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Creating task for project: {} in organization: {}", request.getProjectId(), organizationId);

        String taskKey = sequenceService.generateNextTaskKey(request.getProjectId(), request.getProjectKey());

        UUID reporterId = request.getReporterId();
        if (reporterId == null) {
            reporterId = TenantContext.getUserId();
            if (reporterId == null) {
                throw new BadRequestException("Missing reporter ID or X-User-Id header");
            }
        }

        Task task = Task.builder()
                .organizationId(organizationId)
                .projectId(request.getProjectId())
                .taskKey(taskKey)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .taskType(request.getTaskType())
                .status(TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .assigneeId(request.getAssigneeId())
                .reporterId(reporterId)
                .parentTaskId(request.getParentTaskId())
                .storyPoints(request.getStoryPoints())
                .estimatedHours(request.getEstimatedHours())
                .loggedHours(BigDecimal.ZERO)
                .dueDate(request.getDueDate())
                .sprintId(request.getSprintId())
                .milestoneId(request.getMilestoneId())
                .build();

        Task saved = taskRepository.save(task);
        log.info("Task created successfully with key: {} and ID: {}", saved.getTaskKey(), saved.getId());

        publishEvent(
                TaskEventType.TASK_CREATED,
                organizationId,
                saved.getProjectId(),
                saved.getId(),
                saved.getTaskKey(),
                saved.getTitle(),
                saved.getTaskType().name(),
                saved.getStatus().name(),
                saved.getPriority().name(),
                saved.getAssigneeId(),
                saved.getReporterId(),
                "Task created with key " + saved.getTaskKey()
        );

        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID taskId) {
        UUID organizationId = getRequiredOrganizationId();
        log.debug("Fetching task by ID: {} in organization: {}", taskId, organizationId);

        Task task = findTaskOrThrow(taskId, organizationId);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskByKey(String taskKey) {
        UUID organizationId = getRequiredOrganizationId();
        String normalizedKey = taskKey != null ? taskKey.trim().toUpperCase() : "";
        log.debug("Fetching task by key: {} in organization: {}", normalizedKey, organizationId);

        Task task = taskRepository.findByOrganizationIdAndTaskKey(organizationId, normalizedKey)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with key: " + normalizedKey));

        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Updating task ID: {} in organization: {}", taskId, organizationId);

        Task task = findTaskOrThrow(taskId, organizationId);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getTaskType() != null) {
            task.setTaskType(request.getTaskType());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getAssigneeId() != null) {
            task.setAssigneeId(request.getAssigneeId());
        }
        if (request.getStoryPoints() != null) {
            task.setStoryPoints(request.getStoryPoints());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getLoggedHours() != null) {
            task.setLoggedHours(request.getLoggedHours());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getSprintId() != null) {
            task.setSprintId(request.getSprintId());
        }
        if (request.getMilestoneId() != null) {
            task.setMilestoneId(request.getMilestoneId());
        }

        Task updated = taskRepository.save(task);
        log.info("Task ID: {} updated successfully", taskId);

        publishEvent(
                TaskEventType.TASK_UPDATED,
                organizationId,
                updated.getProjectId(),
                updated.getId(),
                updated.getTaskKey(),
                updated.getTitle(),
                updated.getTaskType().name(),
                updated.getStatus().name(),
                updated.getPriority().name(),
                updated.getAssigneeId(),
                updated.getReporterId(),
                "Task details updated"
        );

        return taskMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(UUID taskId, TaskStatus status) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Updating status of task ID: {} to {}", taskId, status);

        Task task = findTaskOrThrow(taskId, organizationId);
        task.setStatus(status);
        Task updated = taskRepository.save(task);

        publishEvent(
                TaskEventType.TASK_STATUS_CHANGED,
                organizationId,
                updated.getProjectId(),
                updated.getId(),
                updated.getTaskKey(),
                updated.getTitle(),
                updated.getTaskType().name(),
                updated.getStatus().name(),
                updated.getPriority().name(),
                updated.getAssigneeId(),
                updated.getReporterId(),
                "Task status changed to " + status
        );

        return taskMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public TaskResponse assignTask(UUID taskId, UUID assigneeId) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Assigning task ID: {} to user: {}", taskId, assigneeId);

        Task task = findTaskOrThrow(taskId, organizationId);
        task.setAssigneeId(assigneeId);
        Task updated = taskRepository.save(task);

        publishEvent(
                TaskEventType.TASK_ASSIGNED,
                organizationId,
                updated.getProjectId(),
                updated.getId(),
                updated.getTaskKey(),
                updated.getTitle(),
                updated.getTaskType().name(),
                updated.getStatus().name(),
                updated.getPriority().name(),
                assigneeId,
                updated.getReporterId(),
                "Task assigned to user " + assigneeId
        );

        return taskMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> searchTasks(TaskSearchCriteria criteria, Pageable pageable) {
        UUID organizationId = getRequiredOrganizationId();
        log.debug("Searching tasks in organization: {} with criteria: {}", organizationId, criteria);

        Specification<Task> spec = TaskSpecification.buildSpecification(organizationId, criteria);
        Page<Task> page = taskRepository.findAll(spec, pageable);

        List<TaskResponse> content = page.getContent().stream()
                .map(taskMapper::toResponse)
                .toList();

        return PagedResponse.<TaskResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getSubtasks(UUID parentTaskId) {
        UUID organizationId = getRequiredOrganizationId();
        log.debug("Fetching subtasks for parent task: {}", parentTaskId);
        findTaskOrThrow(parentTaskId, organizationId);

        return taskRepository.findByParentTaskId(parentTaskId).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteTask(UUID taskId) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Deleting task ID: {} in organization: {}", taskId, organizationId);

        Task task = findTaskOrThrow(taskId, organizationId);
        taskRepository.delete(task);

        publishEvent(
                TaskEventType.TASK_DELETED,
                organizationId,
                task.getProjectId(),
                task.getId(),
                task.getTaskKey(),
                task.getTitle(),
                task.getTaskType().name(),
                task.getStatus().name(),
                task.getPriority().name(),
                task.getAssigneeId(),
                task.getReporterId(),
                "Task deleted"
        );
    }

    private Task findTaskOrThrow(UUID taskId, UUID organizationId) {
        return taskRepository.findByIdAndOrganizationId(taskId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));
    }

    private UUID getRequiredOrganizationId() {
        UUID orgId = TenantContext.getOrganizationId();
        if (orgId == null) {
            throw new BadRequestException("Missing X-Organization-Id header for tenant isolation");
        }
        return orgId;
    }

    private void publishEvent(
            TaskEventType eventType,
            UUID organizationId,
            UUID projectId,
            UUID taskId,
            String taskKey,
            String title,
            String taskType,
            String status,
            String priority,
            UUID assigneeId,
            UUID reporterId,
            String details
    ) {
        TaskEvent event = TaskEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .organizationId(organizationId)
                .projectId(projectId)
                .taskId(taskId)
                .taskKey(taskKey)
                .title(title)
                .taskType(taskType)
                .status(status)
                .priority(priority)
                .assigneeId(assigneeId)
                .reporterId(reporterId)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
        eventProducer.publish(event);
    }
}

package com.enterprise.platform.task.service.impl;

import com.enterprise.platform.events.TaskEvent;
import com.enterprise.platform.events.TaskEventType;
import com.enterprise.platform.task.context.TenantContext;
import com.enterprise.platform.task.dto.request.CreateTaskCommentRequest;
import com.enterprise.platform.task.dto.response.TaskCommentResponse;
import com.enterprise.platform.task.entity.Task;
import com.enterprise.platform.task.entity.TaskComment;
import com.enterprise.platform.task.exception.BadRequestException;
import com.enterprise.platform.task.exception.ResourceNotFoundException;
import com.enterprise.platform.task.kafka.producer.TaskEventProducer;
import com.enterprise.platform.task.mapper.TaskCommentMapper;
import com.enterprise.platform.task.repository.TaskCommentRepository;
import com.enterprise.platform.task.repository.TaskRepository;
import com.enterprise.platform.task.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository commentRepository;
    private final TaskCommentMapper commentMapper;
    private final TaskEventProducer eventProducer;

    @Override
    @Transactional
    public TaskCommentResponse addComment(UUID taskId, CreateTaskCommentRequest request) {
        UUID organizationId = getRequiredOrganizationId();
        UUID userId = TenantContext.getUserId();
        if (userId == null) {
            throw new BadRequestException("Missing X-User-Id header");
        }

        log.info("Adding comment to task: {} by user: {}", taskId, userId);
        Task task = findTaskOrThrow(taskId, organizationId);

        TaskComment comment = TaskComment.builder()
                .task(task)
                .organizationId(organizationId)
                .userId(userId)
                .comment(request.getComment().trim())
                .build();

        TaskComment saved = commentRepository.save(comment);
        log.info("Comment ID: {} added to task: {}", saved.getId(), taskId);

        TaskEvent event = TaskEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(TaskEventType.TASK_COMMENT_ADDED)
                .organizationId(organizationId)
                .projectId(task.getProjectId())
                .taskId(task.getId())
                .taskKey(task.getTaskKey())
                .title(task.getTitle())
                .taskType(task.getTaskType().name())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .assigneeId(task.getAssigneeId())
                .reporterId(userId)
                .timestamp(LocalDateTime.now())
                .details("Comment added to task")
                .build();
        eventProducer.publish(event);

        return commentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskCommentResponse> getComments(UUID taskId) {
        UUID organizationId = getRequiredOrganizationId();
        log.debug("Fetching comments for task: {}", taskId);
        findTaskOrThrow(taskId, organizationId);

        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteComment(UUID taskId, UUID commentId) {
        UUID organizationId = getRequiredOrganizationId();
        log.info("Deleting comment ID: {} from task: {}", commentId, taskId);
        findTaskOrThrow(taskId, organizationId);

        TaskComment comment = commentRepository.findByIdAndTaskId(commentId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));

        commentRepository.delete(comment);
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
}

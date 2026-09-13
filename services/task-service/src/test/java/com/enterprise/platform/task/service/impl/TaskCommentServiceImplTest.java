package com.enterprise.platform.task.service.impl;

import com.enterprise.platform.events.TaskEvent;
import com.enterprise.platform.events.TaskEventType;
import com.enterprise.platform.task.constants.enums.TaskPriority;
import com.enterprise.platform.task.constants.enums.TaskStatus;
import com.enterprise.platform.task.constants.enums.TaskType;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskCommentServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCommentRepository commentRepository;

    @Mock
    private TaskCommentMapper commentMapper;

    @Mock
    private TaskEventProducer eventProducer;

    @InjectMocks
    private TaskCommentServiceImpl commentService;

    private UUID organizationId;
    private UUID projectId;
    private UUID taskId;
    private UUID commentId;
    private UUID userId;
    private Task task;
    private TaskComment comment;
    private TaskCommentResponse commentResponse;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        userId = UUID.randomUUID();

        TenantContext.setOrganizationId(organizationId);
        TenantContext.setUserId(userId);

        task = Task.builder()
                .id(taskId)
                .organizationId(organizationId)
                .projectId(projectId)
                .taskKey("CORE-1")
                .title("Implement Authentication")
                .taskType(TaskType.STORY)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .reporterId(userId)
                .build();

        comment = TaskComment.builder()
                .id(commentId)
                .task(task)
                .organizationId(organizationId)
                .userId(userId)
                .comment("Looks good, PR opened.")
                .build();

        commentResponse = TaskCommentResponse.builder()
                .id(commentId)
                .taskId(taskId)
                .organizationId(organizationId)
                .userId(userId)
                .comment("Looks good, PR opened.")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should add comment to task and publish TASK_COMMENT_ADDED event")
    void testAddComment_Success() {
        CreateTaskCommentRequest request = new CreateTaskCommentRequest("Looks good, PR opened.");

        when(taskRepository.findByIdAndOrganizationId(taskId, organizationId)).thenReturn(Optional.of(task));
        when(commentRepository.save(any(TaskComment.class))).thenReturn(comment);
        when(commentMapper.toResponse(comment)).thenReturn(commentResponse);

        TaskCommentResponse response = commentService.addComment(taskId, request);

        assertThat(response).isNotNull();
        assertThat(response.getComment()).isEqualTo("Looks good, PR opened.");

        verify(commentRepository).save(any(TaskComment.class));
        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(TaskEventType.TASK_COMMENT_ADDED);
    }

    @Test
    @DisplayName("Should throw BadRequestException when user ID is missing")
    void testAddComment_MissingUserId() {
        TenantContext.setUserId(null);
        CreateTaskCommentRequest request = new CreateTaskCommentRequest("Test comment");

        assertThatThrownBy(() -> commentService.addComment(taskId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Missing X-User-Id header");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when task not found")
    void testAddComment_TaskNotFound() {
        CreateTaskCommentRequest request = new CreateTaskCommentRequest("Test comment");
        when(taskRepository.findByIdAndOrganizationId(taskId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.addComment(taskId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    @DisplayName("Should get all comments for task")
    void testGetComments_Success() {
        when(taskRepository.findByIdAndOrganizationId(taskId, organizationId)).thenReturn(Optional.of(task));
        when(commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)).thenReturn(List.of(comment));
        when(commentMapper.toResponse(comment)).thenReturn(commentResponse);

        List<TaskCommentResponse> comments = commentService.getComments(taskId);

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getComment()).isEqualTo("Looks good, PR opened.");
    }

    @Test
    @DisplayName("Should delete comment successfully")
    void testDeleteComment_Success() {
        when(taskRepository.findByIdAndOrganizationId(taskId, organizationId)).thenReturn(Optional.of(task));
        when(commentRepository.findByIdAndTaskId(commentId, taskId)).thenReturn(Optional.of(comment));

        commentService.deleteComment(taskId, commentId);

        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent comment")
    void testDeleteComment_NotFound() {
        when(taskRepository.findByIdAndOrganizationId(taskId, organizationId)).thenReturn(Optional.of(task));
        when(commentRepository.findByIdAndTaskId(commentId, taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(taskId, commentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found");

        verify(commentRepository, never()).delete(any());
    }
}

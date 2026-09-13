package com.enterprise.platform.task.service.impl;

import com.enterprise.platform.events.TaskEvent;
import com.enterprise.platform.events.TaskEventType;
import com.enterprise.platform.task.constants.enums.TaskPriority;
import com.enterprise.platform.task.constants.enums.TaskStatus;
import com.enterprise.platform.task.constants.enums.TaskType;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskSequenceService sequenceService;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskEventProducer eventProducer;

    @InjectMocks
    private TaskServiceImpl taskService;

    private UUID organizationId;
    private UUID projectId;
    private UUID taskId;
    private UUID userId;
    private Task task;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        userId = UUID.randomUUID();

        TenantContext.setOrganizationId(organizationId);
        TenantContext.setUserId(userId);

        task = Task.builder()
                .id(taskId)
                .organizationId(organizationId)
                .projectId(projectId)
                .taskKey("CORE-1")
                .title("Implement Authentication")
                .description("JWT based auth implementation")
                .taskType(TaskType.STORY)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .assigneeId(userId)
                .reporterId(userId)
                .storyPoints(5)
                .estimatedHours(BigDecimal.valueOf(16))
                .loggedHours(BigDecimal.ZERO)
                .dueDate(LocalDate.now().plusDays(14))
                .build();

        taskResponse = TaskResponse.builder()
                .id(taskId)
                .organizationId(organizationId)
                .projectId(projectId)
                .taskKey("CORE-1")
                .title("Implement Authentication")
                .description("JWT based auth implementation")
                .taskType(TaskType.STORY)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .assigneeId(userId)
                .reporterId(userId)
                .storyPoints(5)
                .estimatedHours(BigDecimal.valueOf(16))
                .loggedHours(BigDecimal.ZERO)
                .dueDate(LocalDate.now().plusDays(14))
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should create task successfully and publish TASK_CREATED event")
    void testCreateTask_Success() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .projectId(projectId)
                .projectKey("CORE")
                .title("Implement Authentication")
                .description("JWT based auth implementation")
                .taskType(TaskType.STORY)
                .priority(TaskPriority.HIGH)
                .assigneeId(userId)
                .storyPoints(5)
                .estimatedHours(BigDecimal.valueOf(16))
                .dueDate(LocalDate.now().plusDays(14))
                .build();

        when(sequenceService.generateNextTaskKey(projectId, "CORE")).thenReturn("CORE-1");
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        TaskResponse response = taskService.createTask(request);

        assertThat(response).isNotNull();
        assertThat(response.getTaskKey()).isEqualTo("CORE-1");
        assertThat(response.getTitle()).isEqualTo("Implement Authentication");

        verify(taskRepository).save(any(Task.class));
        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        TaskEvent event = eventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo(TaskEventType.TASK_CREATED);
        assertThat(event.getTaskKey()).isEqualTo("CORE-1");
        assertThat(event.getOrganizationId()).isEqualTo(organizationId);
    }

    @Test
    @DisplayName("Should throw BadRequestException when tenant header is missing")
    void testCreateTask_MissingTenantHeader() {
        TenantContext.clear();
        CreateTaskRequest request = CreateTaskRequest.builder()
                .projectId(projectId)
                .projectKey("CORE")
                .title("Implement Authentication")
                .taskType(TaskType.STORY)
                .build();

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Missing X-Organization-Id");
    }

    @Test
    @DisplayName("Should get task by ID successfully")
    void testGetTaskById_Success() {
        when(taskRepository.findByIdAndOrganizationId(taskId, organizationId)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        TaskResponse response = taskService.getTaskById(taskId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(taskId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when task not found by ID")
    void testGetTaskById_NotFound() {
        when(taskRepository.findByIdAndOrganizationId(taskId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(taskId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    @DisplayName("Should get task by key successfully")
    void testGetTaskByKey_Success() {
        when(taskRepository.findByOrganizationIdAndTaskKey(organizationId, "CORE-1")).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        TaskResponse response = taskService.getTaskByKey("core-1");

        assertThat(response).isNotNull();
        assertThat(response.getTaskKey()).isEqualTo("CORE-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when task not found by key")
    void testGetTaskByKey_NotFound() {
        when(taskRepository.findByOrganizationIdAndTaskKey(organizationId, "CORE-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskByKey("CORE-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    @DisplayName("Should update task details and publish TASK_UPDATED event")
    void testUpdateTask_Success() {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Updated Title")
                .priority(TaskPriority.URGENT)
                .build();

        when(taskRepository.findByIdAndOrganizationId(taskId, organizationId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        TaskResponse response = taskService.updateTask(taskId, request);

        assertThat(response).isNotNull();
        verify(taskRepository).save(task);
        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(TaskEventType.TASK_UPDATED);
    }

    @Test
    @DisplayName("Should update task status and publish TASK_STATUS_CHANGED event")
    void testUpdateTaskStatus_Success() {
        when(taskRepository.findByIdAndOrganizationId(taskId, organizationId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        TaskResponse response = taskService.updateTaskStatus(taskId, TaskStatus.IN_PROGRESS);

        assertThat(response).isNotNull();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository).save(task);

        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(TaskEventType.TASK_STATUS_CHANGED);
    }

    @Test
    @DisplayName("Should assign task to user and publish TASK_ASSIGNED event")
    void testAssignTask_Success() {
        UUID newAssignee = UUID.randomUUID();
        when(taskRepository.findByIdAndOrganizationId(taskId, organizationId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        TaskResponse response = taskService.assignTask(taskId, newAssignee);

        assertThat(response).isNotNull();
        assertThat(task.getAssigneeId()).isEqualTo(newAssignee);
        verify(taskRepository).save(task);

        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(TaskEventType.TASK_ASSIGNED);
    }

    @Test
    @DisplayName("Should search tasks with criteria and pagination")
    void testSearchTasks_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(List.of(task), pageable, 1);
        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .projectId(projectId)
                .status(TaskStatus.TODO)
                .build();

        when(taskRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        PagedResponse<TaskResponse> response = taskService.searchTasks(criteria, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should get subtasks for parent task")
    void testGetSubtasks_Success() {
        Task subtask = Task.builder()
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .projectId(projectId)
                .taskKey("CORE-2")
                .title("Write unit tests")
                .taskType(TaskType.SUBTASK)
                .parentTaskId(taskId)
                .build();

        when(taskRepository.findByIdAndOrganizationId(taskId, organizationId)).thenReturn(Optional.of(task));
        when(taskRepository.findByParentTaskId(taskId)).thenReturn(List.of(subtask));
        when(taskMapper.toResponse(subtask)).thenReturn(taskResponse);

        List<TaskResponse> subtasks = taskService.getSubtasks(taskId);

        assertThat(subtasks).hasSize(1);
    }

    @Test
    @DisplayName("Should delete task successfully and publish TASK_DELETED event")
    void testDeleteTask_Success() {
        when(taskRepository.findByIdAndOrganizationId(taskId, organizationId)).thenReturn(Optional.of(task));

        taskService.deleteTask(taskId);

        verify(taskRepository).delete(task);
        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(TaskEventType.TASK_DELETED);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent task")
    void testDeleteTask_NotFound() {
        when(taskRepository.findByIdAndOrganizationId(taskId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(taskId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");

        verify(taskRepository, never()).delete(any(Task.class));
    }
}

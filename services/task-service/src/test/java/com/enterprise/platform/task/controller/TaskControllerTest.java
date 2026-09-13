package com.enterprise.platform.task.controller;

import com.enterprise.platform.task.constants.enums.TaskPriority;
import com.enterprise.platform.task.constants.enums.TaskStatus;
import com.enterprise.platform.task.constants.enums.TaskType;
import com.enterprise.platform.task.dto.request.*;
import com.enterprise.platform.task.dto.response.PagedResponse;
import com.enterprise.platform.task.dto.response.TaskResponse;
import com.enterprise.platform.task.exception.GlobalExceptionHandler;
import com.enterprise.platform.task.exception.ResourceNotFoundException;
import com.enterprise.platform.task.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID taskId;
    private UUID projectId;
    private UUID organizationId;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        taskId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        organizationId = UUID.randomUUID();

        taskResponse = TaskResponse.builder()
                .id(taskId)
                .organizationId(organizationId)
                .projectId(projectId)
                .taskKey("CORE-1")
                .title("Implement Authentication")
                .description("JWT auth")
                .taskType(TaskType.STORY)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .assigneeId(UUID.randomUUID())
                .reporterId(UUID.randomUUID())
                .storyPoints(5)
                .estimatedHours(BigDecimal.valueOf(16))
                .loggedHours(BigDecimal.ZERO)
                .dueDate(LocalDate.now().plusDays(7))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/tasks - Success returns 201 CREATED")
    void testCreateTask_Success() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .projectId(projectId)
                .projectKey("CORE")
                .title("Implement Authentication")
                .taskType(TaskType.STORY)
                .priority(TaskPriority.HIGH)
                .build();

        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(taskResponse);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.taskKey").value("CORE-1"))
                .andExpect(jsonPath("$.data.title").value("Implement Authentication"));
    }

    @Test
    @DisplayName("POST /api/v1/tasks - Validation failure returns 400")
    void testCreateTask_ValidationFailure() throws Exception {
        CreateTaskRequest invalidRequest = CreateTaskRequest.builder()
                .projectId(null) // Missing projectId
                .title("") // Blank title
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id} - Success returns 200 OK")
    void testGetTaskById_Success() throws Exception {
        when(taskService.getTaskById(taskId)).thenReturn(taskResponse);

        mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(taskId.toString()))
                .andExpect(jsonPath("$.data.taskKey").value("CORE-1"));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id} - Not found returns 404")
    void testGetTaskById_NotFound() throws Exception {
        when(taskService.getTaskById(taskId))
                .thenThrow(new ResourceNotFoundException("Task not found with ID: " + taskId));

        mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/key/{taskKey} - Success returns 200 OK")
    void testGetTaskByKey_Success() throws Exception {
        when(taskService.getTaskByKey("CORE-1")).thenReturn(taskResponse);

        mockMvc.perform(get("/api/v1/tasks/key/{taskKey}", "CORE-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.taskKey").value("CORE-1"));
    }

    @Test
    @DisplayName("PUT /api/v1/tasks/{id} - Success returns 200 OK")
    void testUpdateTask_Success() throws Exception {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Updated Title")
                .priority(TaskPriority.URGENT)
                .build();

        when(taskService.updateTask(eq(taskId), any(UpdateTaskRequest.class))).thenReturn(taskResponse);

        mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task updated successfully"));
    }

    @Test
    @DisplayName("PATCH /api/v1/tasks/{id}/status - Success returns 200 OK")
    void testUpdateTaskStatus_Success() throws Exception {
        TaskStatusUpdateRequest request = new TaskStatusUpdateRequest(TaskStatus.IN_PROGRESS);

        when(taskService.updateTaskStatus(taskId, TaskStatus.IN_PROGRESS)).thenReturn(taskResponse);

        mockMvc.perform(patch("/api/v1/tasks/{id}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task status updated successfully"));
    }

    @Test
    @DisplayName("PATCH /api/v1/tasks/{id}/assign - Success returns 200 OK")
    void testAssignTask_Success() throws Exception {
        UUID newAssignee = UUID.randomUUID();
        TaskAssignRequest request = new TaskAssignRequest(newAssignee);

        when(taskService.assignTask(taskId, newAssignee)).thenReturn(taskResponse);

        mockMvc.perform(patch("/api/v1/tasks/{id}/assign", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task assigned successfully"));
    }

    @Test
    @DisplayName("GET /api/v1/tasks - Search returns 200 OK with PagedResponse")
    void testSearchTasks_Success() throws Exception {
        PagedResponse<TaskResponse> pagedResponse = PagedResponse.<TaskResponse>builder()
                .content(List.of(taskResponse))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(taskService.searchTasks(any(TaskSearchCriteria.class), any(Pageable.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/tasks")
                        .param("projectId", projectId.toString())
                        .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].taskKey").value("CORE-1"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id}/subtasks - Success returns 200 OK")
    void testGetSubtasks_Success() throws Exception {
        when(taskService.getSubtasks(taskId)).thenReturn(List.of(taskResponse));

        mockMvc.perform(get("/api/v1/tasks/{id}/subtasks", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].taskKey").value("CORE-1"));
    }

    @Test
    @DisplayName("DELETE /api/v1/tasks/{id} - Success returns 200 OK")
    void testDeleteTask_Success() throws Exception {
        doNothing().when(taskService).deleteTask(taskId);

        mockMvc.perform(delete("/api/v1/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task deleted successfully"));
    }
}

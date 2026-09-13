package com.enterprise.platform.task.controller;

import com.enterprise.platform.task.constants.enums.TaskPriority;
import com.enterprise.platform.task.constants.enums.TaskStatus;
import com.enterprise.platform.task.constants.enums.TaskType;
import com.enterprise.platform.task.dto.request.*;
import com.enterprise.platform.task.dto.response.ApiResponse;
import com.enterprise.platform.task.dto.response.PagedResponse;
import com.enterprise.platform.task.dto.response.TaskResponse;
import com.enterprise.platform.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "APIs for tasks, issues, stories, and epics")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody CreateTaskRequest request
    ) {
        log.info("REST request to create task: '{}' for project: {}", request.getTitle(), request.getProjectId());
        TaskResponse response = taskService.createTask(request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Task created successfully"),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @PathVariable UUID id
    ) {
        log.info("REST request to get task by ID: {}", id);
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/key/{taskKey}")
    @Operation(summary = "Get task by task key (e.g. CORE-1)")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskByKey(
            @PathVariable String taskKey
    ) {
        log.info("REST request to get task by key: {}", taskKey);
        TaskResponse response = taskService.getTaskByKey(taskKey);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task details")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        log.info("REST request to update task ID: {}", id);
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Task updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TaskStatusUpdateRequest request
    ) {
        log.info("REST request to update status of task ID: {} to {}", id, request.getStatus());
        TaskResponse response = taskService.updateTaskStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(response, "Task status updated successfully"));
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign task to user")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable UUID id,
            @Valid @RequestBody TaskAssignRequest request
    ) {
        log.info("REST request to assign task ID: {} to user: {}", id, request.getAssigneeId());
        TaskResponse response = taskService.assignTask(id, request.getAssigneeId());
        return ResponseEntity.ok(ApiResponse.success(response, "Task assigned successfully"));
    }

    @GetMapping
    @Operation(summary = "Search and list tasks with filters and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> searchTasks(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskType taskType,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) UUID reporterId,
            @RequestParam(required = false) UUID sprintId,
            @RequestParam(required = false) UUID parentTaskId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("REST request to search tasks with criteria");
        TaskSearchCriteria criteria = TaskSearchCriteria.builder()
                .projectId(projectId)
                .search(search)
                .status(status)
                .taskType(taskType)
                .priority(priority)
                .assigneeId(assigneeId)
                .reporterId(reporterId)
                .sprintId(sprintId)
                .parentTaskId(parentTaskId)
                .build();

        PagedResponse<TaskResponse> response = taskService.searchTasks(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/subtasks")
    @Operation(summary = "Get subtasks for a parent task")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getSubtasks(
            @PathVariable UUID id
    ) {
        log.info("REST request to get subtasks for task ID: {}", id);
        List<TaskResponse> response = taskService.getSubtasks(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable UUID id
    ) {
        log.info("REST request to delete task ID: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Task deleted successfully"));
    }
}

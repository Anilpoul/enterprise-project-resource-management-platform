package com.enterprise.platform.task.service;

import com.enterprise.platform.task.constants.enums.TaskStatus;
import com.enterprise.platform.task.dto.request.CreateTaskRequest;
import com.enterprise.platform.task.dto.request.TaskSearchCriteria;
import com.enterprise.platform.task.dto.request.UpdateTaskRequest;
import com.enterprise.platform.task.dto.response.PagedResponse;
import com.enterprise.platform.task.dto.response.TaskResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskResponse createTask(CreateTaskRequest request);

    TaskResponse getTaskById(UUID taskId);

    TaskResponse getTaskByKey(String taskKey);

    TaskResponse updateTask(UUID taskId, UpdateTaskRequest request);

    TaskResponse updateTaskStatus(UUID taskId, TaskStatus status);

    TaskResponse assignTask(UUID taskId, UUID assigneeId);

    PagedResponse<TaskResponse> searchTasks(TaskSearchCriteria criteria, Pageable pageable);

    List<TaskResponse> getSubtasks(UUID parentTaskId);

    void deleteTask(UUID taskId);
}

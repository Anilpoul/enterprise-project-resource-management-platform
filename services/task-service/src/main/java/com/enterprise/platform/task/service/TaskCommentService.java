package com.enterprise.platform.task.service;

import com.enterprise.platform.task.dto.request.CreateTaskCommentRequest;
import com.enterprise.platform.task.dto.response.TaskCommentResponse;

import java.util.List;
import java.util.UUID;

public interface TaskCommentService {

    TaskCommentResponse addComment(UUID taskId, CreateTaskCommentRequest request);

    List<TaskCommentResponse> getComments(UUID taskId);

    void deleteComment(UUID taskId, UUID commentId);
}
